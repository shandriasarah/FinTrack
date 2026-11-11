package com.example.fintrack

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/** Quanto dá para gastar por dia sem comprometer contas e metas. */
data class LivreParaGastar(
    val porDia: Double,
    val disponivel: Double,
    val diasRestantes: Int,
    val contasPendentes: Double,
    val faltaGuardar: Double
)

enum class TipoInsight { POSITIVO, ALERTA, DICA }

data class Insight(val emoji: String, val texto: String, val tipo: TipoInsight)

class FinancasViewModel(application: Application) : AndroidViewModel(application) {

    private val armazenamento = Armazenamento(application)

    val transacoes = mutableStateListOf<Transacao>()
    val contas = mutableStateListOf<ContaFixa>()
    val metas = mutableStateListOf<Meta>()

    var nomeUsuario by mutableStateOf(armazenamento.nomeUsuario)
        private set
    var percentualReserva by mutableIntStateOf(armazenamento.percentualReserva)
        private set
    var ocultarValores by mutableStateOf(armazenamento.ocultarValores)
        private set

    /** Mês que está sendo exibido (formato de [chaveMes]). */
    var mesSelecionado by mutableIntStateOf(chaveMesAtual())
        private set

    private var ultimoId = 0L

    init {
        if (armazenamento.primeiraExecucao) {
            carregarDadosDeExemplo()
            armazenamento.primeiraExecucao = false
        } else {
            transacoes.addAll(armazenamento.carregarTransacoes().sortedByDescending { it.data })
            contas.addAll(armazenamento.carregarContas())
            metas.addAll(armazenamento.carregarMetas())
        }
    }

    // =====================================================================
    // Navegação entre meses
    // =====================================================================

    val ehMesAtual: Boolean get() = mesSelecionado == chaveMesAtual()

    fun mesAnterior() {
        mesSelecionado -= 1
    }

    fun proximoMes() {
        if (mesSelecionado < chaveMesAtual()) mesSelecionado += 1
    }

    // =====================================================================
    // Consultas
    // =====================================================================

    fun transacoesDoMes(mes: Int = mesSelecionado): List<Transacao> =
        transacoes.filter { chaveMes(it.data) == mes }

    fun totalEntradas(mes: Int = mesSelecionado): Double =
        transacoesDoMes(mes).filter { it.tipo == TipoTransacao.ENTRADA }.sumOf { it.valor }

    /** Gastos do mês (sem contar o dinheiro guardado em metas). */
    fun totalGastos(mes: Int = mesSelecionado): Double =
        transacoesDoMes(mes)
            .filter { it.tipo == TipoTransacao.SAIDA && it.categoria != Categoria.RESERVA }
            .sumOf { it.valor }

    fun totalGuardado(mes: Int = mesSelecionado): Double =
        transacoesDoMes(mes).filter { it.categoria == Categoria.RESERVA }.sumOf { it.valor }

    fun saldoDoMes(mes: Int = mesSelecionado): Double =
        totalEntradas(mes) - totalGastos(mes) - totalGuardado(mes)

    /** Gastos agrupados por categoria, do maior para o menor. */
    fun gastosPorCategoria(mes: Int = mesSelecionado): List<Pair<Categoria, Double>> =
        transacoesDoMes(mes)
            .filter { it.tipo == TipoTransacao.SAIDA && it.categoria != Categoria.RESERVA }
            .groupBy { it.categoria }
            .map { (cat, lista) -> cat to lista.sumOf { it.valor } }
            .sortedByDescending { it.second }

    fun gastosPorGrupo(mes: Int = mesSelecionado): Map<GrupoOrcamento, Double> =
        transacoesDoMes(mes)
            .filter { it.tipo == TipoTransacao.SAIDA }
            .groupBy { it.categoria.grupo }
            .mapValues { (_, lista) -> lista.sumOf { it.valor } }

    /** Percentual ideal de cada grupo, ajustado pelo quanto a pessoa quer guardar. */
    fun metaDoGrupo(grupo: GrupoOrcamento): Int = when (grupo) {
        GrupoOrcamento.ESSENCIAL -> 50
        GrupoOrcamento.FUTURO -> percentualReserva
        GrupoOrcamento.ESTILO_DE_VIDA -> max(0, 100 - 50 - percentualReserva)
        GrupoOrcamento.RENDA -> 100
    }

    fun contaPaga(conta: ContaFixa, mes: Int = mesSelecionado): Boolean =
        transacoes.any { it.contaId == conta.id && chaveMes(it.data) == mes }

    fun contasPendentes(mes: Int = mesSelecionado): List<ContaFixa> =
        contas.filter { !contaPaga(it, mes) }.sortedBy { it.diaVencimento }

    fun guardadoNaMeta(meta: Meta): Double =
        transacoes.filter { it.metaId == meta.id }.sumOf { it.valor }

    fun livreParaGastarHoje(): LivreParaGastar? {
        if (!ehMesAtual) return null
        val mes = mesSelecionado
        val entradas = totalEntradas(mes)
        if (entradas <= 0.0) return null

        val diasRestantes = diasNoMes(mes) - diaDoMes(System.currentTimeMillis()) + 1
        val pendentes = contasPendentes(mes).sumOf { it.valor }
        val metaGuardar = entradas * percentualReserva / 100.0
        val faltaGuardar = max(0.0, metaGuardar - totalGuardado(mes))
        val disponivel = saldoDoMes(mes) - pendentes - faltaGuardar

        return LivreParaGastar(
            porDia = disponivel / max(1, diasRestantes),
            disponivel = disponivel,
            diasRestantes = diasRestantes,
            contasPendentes = pendentes,
            faltaGuardar = faltaGuardar
        )
    }

    /** Frases curtas geradas a partir dos dados do mês. */
    fun insights(mes: Int = mesSelecionado): List<Insight> {
        val lista = mutableListOf<Insight>()
        val entradas = totalEntradas(mes)
        val gastos = totalGastos(mes)

        if (transacoesDoMes(mes).isEmpty()) {
            lista += Insight("✍️", "Nenhum lançamento neste mês. Toque no + para registrar o primeiro.", TipoInsight.DICA)
            return lista
        }

        // Comparação com o mês anterior (proporcional ao dia, se for o mês atual)
        val gastosMesPassado = totalGastos(mes - 1)
        if (gastosMesPassado > 0) {
            val referencia = if (mes == chaveMesAtual()) {
                gastosMesPassado * diaDoMes(System.currentTimeMillis()) / diasNoMes(mes - 1)
            } else {
                gastosMesPassado
            }
            if (referencia > 0) {
                val variacao = ((gastos - referencia) / referencia * 100).roundToInt()
                if (variacao <= -5) {
                    lista += Insight("🎉", "Você está gastando ${abs(variacao)}% menos que no mesmo período do mês passado.", TipoInsight.POSITIVO)
                } else if (variacao >= 10) {
                    lista += Insight("📊", "Seus gastos estão $variacao% acima do mesmo período do mês passado.", TipoInsight.ALERTA)
                }
            }
        }

        // Maior categoria
        val porCategoria = gastosPorCategoria(mes)
        if (porCategoria.isNotEmpty() && gastos > 0) {
            val (cat, valor) = porCategoria.first()
            val pct = (valor / gastos * 100).roundToInt()
            if (pct >= 25) {
                lista += Insight(cat.emoji, "${cat.nome} é sua maior despesa: $pct% de tudo que você gastou.", TipoInsight.DICA)
            }
        }

        // Regra 50/30/20
        if (entradas > 0) {
            val estilo = gastosPorGrupo(mes)[GrupoOrcamento.ESTILO_DE_VIDA] ?: 0.0
            val pctEstilo = (estilo / entradas * 100).roundToInt()
            val limiteEstilo = metaDoGrupo(GrupoOrcamento.ESTILO_DE_VIDA)
            if (pctEstilo > limiteEstilo) {
                lista += Insight("⚠️", "Estilo de vida já usou $pctEstilo% da sua renda (o ideal é até $limiteEstilo%).", TipoInsight.ALERTA)
            }

            val guardado = totalGuardado(mes)
            val metaGuardar = entradas * percentualReserva / 100.0
            if (guardado >= metaGuardar && metaGuardar > 0) {
                lista += Insight("🐷", "Meta do mês batida! Você já guardou ${formatarMoeda(guardado)}.", TipoInsight.POSITIVO)
            } else if (metaGuardar > 0) {
                lista += Insight("🐷", "Faltam ${formatarMoeda(metaGuardar - guardado)} para guardar $percentualReserva% da renda este mês.", TipoInsight.DICA)
            }
        } else {
            lista += Insight("💼", "Registre suas entradas para ver quanto pode gastar por dia.", TipoInsight.DICA)
        }

        // Contas vencendo
        if (mes == chaveMesAtual()) {
            val hoje = diaDoMes(System.currentTimeMillis())
            contasPendentes(mes).firstOrNull { it.diaVencimento in hoje..(hoje + 3) }?.let { conta ->
                val dias = conta.diaVencimento - hoje
                val quando = when (dias) {
                    0 -> "vence hoje"
                    1 -> "vence amanhã"
                    else -> "vence em $dias dias"
                }
                lista += Insight("⏰", "${conta.nome} (${formatarMoeda(conta.valor)}) $quando.", TipoInsight.ALERTA)
            }
            contasPendentes(mes).firstOrNull { it.diaVencimento < hoje }?.let { conta ->
                lista += Insight("❗", "${conta.nome} venceu dia ${conta.diaVencimento} e ainda não foi marcada como paga.", TipoInsight.ALERTA)
            }
        }

        return lista
    }

    // =====================================================================
    // Ações
    // =====================================================================

    fun adicionarTransacao(
        titulo: String,
        valor: Double,
        tipo: TipoTransacao,
        categoria: Categoria,
        data: Long = System.currentTimeMillis(),
        contaId: Long? = null,
        metaId: Long? = null
    ) {
        val nova = Transacao(novoId(), titulo.trim(), valor, tipo, categoria, data, contaId, metaId)
        val posicao = transacoes.indexOfFirst { it.data < data }.let { if (it == -1) transacoes.size else it }
        transacoes.add(posicao, nova)
        salvarTransacoes()
    }

    fun removerTransacao(transacao: Transacao) {
        transacoes.remove(transacao)
        salvarTransacoes()
    }

    fun adicionarConta(nome: String, valor: Double, dia: Int, categoria: Categoria) {
        contas.add(ContaFixa(novoId(), nome.trim(), valor, dia.coerceIn(1, 31), categoria))
        armazenamento.salvarContas(contas)
    }

    fun removerConta(conta: ContaFixa) {
        contas.remove(conta)
        armazenamento.salvarContas(contas)
    }

    /** Marca a conta como paga no mês selecionado (ou desfaz o pagamento). */
    fun alternarPagamento(conta: ContaFixa) {
        val mes = mesSelecionado
        val pagamento = transacoes.firstOrNull { it.contaId == conta.id && chaveMes(it.data) == mes }
        if (pagamento != null) {
            removerTransacao(pagamento)
        } else {
            val data = if (mes == chaveMesAtual()) System.currentTimeMillis() else dataNoMes(mes, conta.diaVencimento)
            adicionarTransacao(conta.nome, conta.valor, TipoTransacao.SAIDA, conta.categoria, data, contaId = conta.id)
        }
    }

    fun adicionarMeta(nome: String, emoji: String, valorAlvo: Double) {
        metas.add(Meta(novoId(), nome.trim(), emoji, valorAlvo))
        armazenamento.salvarMetas(metas)
    }

    fun removerMeta(meta: Meta) {
        metas.remove(meta)
        armazenamento.salvarMetas(metas)
    }

    fun guardarNaMeta(meta: Meta, valor: Double) {
        adicionarTransacao("Guardado: ${meta.nome}", valor, TipoTransacao.SAIDA, Categoria.RESERVA, metaId = meta.id)
    }

    fun atualizarNome(nome: String) {
        nomeUsuario = nome.trim().ifEmpty { "Você" }
        armazenamento.nomeUsuario = nomeUsuario
    }

    fun atualizarPercentualReserva(percentual: Int) {
        percentualReserva = percentual.coerceIn(0, 50)
        armazenamento.percentualReserva = percentualReserva
    }

    fun alternarOcultarValores() {
        ocultarValores = !ocultarValores
        armazenamento.ocultarValores = ocultarValores
    }

    fun apagarTudo() {
        transacoes.clear()
        contas.clear()
        metas.clear()
        armazenamento.apagarTudo()
    }

    fun carregarDadosDeExemplo() {
        transacoes.clear()
        contas.clear()
        metas.clear()

        val atual = chaveMesAtual()
        val anterior = atual - 1
        val hoje = diaDoMes(System.currentTimeMillis())

        val aluguel = ContaFixa(novoId(), "Aluguel", 1200.0, 5, Categoria.MORADIA)
        val internet = ContaFixa(novoId(), "Internet", 120.0, 15, Categoria.CONTAS)
        val luz = ContaFixa(novoId(), "Conta de luz", 185.50, 20, Categoria.CONTAS)
        val streaming = ContaFixa(novoId(), "Streaming", 39.90, hoje.coerceAtMost(27) + 1, Categoria.ASSINATURAS)
        val academia = ContaFixa(novoId(), "Academia", 89.90, 10, Categoria.SAUDE)
        contas.addAll(listOf(aluguel, internet, luz, streaming, academia))

        val reserva = Meta(novoId(), "Reserva de emergência", "🛟", 10000.0)
        val viagem = Meta(novoId(), "Viagem de férias", "✈️", 4000.0)
        metas.addAll(listOf(reserva, viagem))

        val lista = mutableListOf<Transacao>()
        fun t(mes: Int, dia: Int, titulo: String, valor: Double, cat: Categoria, conta: Long? = null, meta: Long? = null) {
            lista += Transacao(novoId(), titulo, valor, cat.tipo, cat, dataNoMes(mes, dia), conta, meta)
        }

        // Mês passado (completo)
        t(anterior, 5, "Salário", 4200.0, Categoria.SALARIO)
        t(anterior, 5, aluguel.nome, aluguel.valor, aluguel.categoria, conta = aluguel.id)
        t(anterior, 7, "Guardado: ${reserva.nome}", 600.0, Categoria.RESERVA, meta = reserva.id)
        t(anterior, 8, "Supermercado", 412.30, Categoria.MERCADO)
        t(anterior, 10, academia.nome, academia.valor, academia.categoria, conta = academia.id)
        t(anterior, 12, "Jantar fora", 148.0, Categoria.RESTAURANTE)
        t(anterior, 14, "Uber", 46.80, Categoria.TRANSPORTE)
        t(anterior, 15, internet.nome, internet.valor, internet.categoria, conta = internet.id)
        t(anterior, 18, "Freelance site", 900.0, Categoria.FREELANCE)
        t(anterior, 19, streaming.nome, streaming.valor, streaming.categoria, conta = streaming.id)
        t(anterior, 20, luz.nome, 172.40, luz.categoria, conta = luz.id)
        t(anterior, 21, "Cinema", 64.0, Categoria.LAZER)
        t(anterior, 23, "Roupas", 289.90, Categoria.COMPRAS)
        t(anterior, 25, "Feira", 98.50, Categoria.MERCADO)
        t(anterior, 26, "Delivery", 72.90, Categoria.RESTAURANTE)
        t(anterior, 28, "Guardado: ${viagem.nome}", 300.0, Categoria.RESERVA, meta = viagem.id)

        // Mês atual (só até hoje)
        fun tAtual(dia: Int, titulo: String, valor: Double, cat: Categoria, conta: ContaFixa? = null, meta: Long? = null) {
            if (dia <= hoje) t(atual, dia, titulo, valor, cat, conta?.id, meta)
        }
        tAtual(1, "Salário", 4200.0, Categoria.SALARIO)
        tAtual(2, "Supermercado", 356.70, Categoria.MERCADO)
        tAtual(3, "Café com amigas", 38.50, Categoria.RESTAURANTE)
        tAtual(5, aluguel.nome, aluguel.valor, aluguel.categoria, conta = aluguel)
        tAtual(6, "Guardado: ${reserva.nome}", 400.0, Categoria.RESERVA, meta = reserva.id)
        tAtual(7, "Combustível", 150.0, Categoria.TRANSPORTE)
        tAtual(9, "Farmácia", 54.20, Categoria.SAUDE)
        tAtual(10, academia.nome, academia.valor, academia.categoria, conta = academia)
        tAtual(11, "Restaurante", 96.0, Categoria.RESTAURANTE)
        tAtual(13, "Curso online", 79.90, Categoria.EDUCACAO)
        tAtual(15, internet.nome, internet.valor, internet.categoria, conta = internet)
        tAtual(16, "Freelance logo", 650.0, Categoria.FREELANCE)
        tAtual(17, "Show", 180.0, Categoria.LAZER)
        tAtual(19, "Supermercado", 288.40, Categoria.MERCADO)
        tAtual(22, "Presente", 120.0, Categoria.COMPRAS)
        tAtual(24, "Uber", 32.60, Categoria.TRANSPORTE)

        transacoes.addAll(lista.sortedByDescending { it.data })
        mesSelecionado = atual
        salvarTudo()
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    private fun novoId(): Long {
        val candidato = max(System.currentTimeMillis(), ultimoId + 1)
        val maiorExistente = (transacoes.maxOfOrNull { it.id } ?: 0L)
            .coerceAtLeast(contas.maxOfOrNull { it.id } ?: 0L)
            .coerceAtLeast(metas.maxOfOrNull { it.id } ?: 0L)
        ultimoId = max(candidato, maiorExistente + 1)
        return ultimoId
    }

    private fun salvarTransacoes() = armazenamento.salvarTransacoes(transacoes)

    private fun salvarTudo() {
        armazenamento.salvarTransacoes(transacoes)
        armazenamento.salvarContas(contas)
        armazenamento.salvarMetas(metas)
    }
}
