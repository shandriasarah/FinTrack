package com.example.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fintrack.ui.theme.FinTrackCores
import com.example.fintrack.ui.theme.GradienteFim
import com.example.fintrack.ui.theme.GradienteInicio
import com.example.fintrack.ui.theme.Verde80
import kotlin.math.roundToInt

@Composable
fun InicioView(
    viewModel: FinancasViewModel,
    onVerExtrato: () -> Unit,
    onVerPlanejamento: () -> Unit
) {
    val mes = viewModel.mesSelecionado
    val oculto = viewModel.ocultarValores

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ---------- Cabeçalho ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Olá, ${viewModel.nomeUsuario.substringBefore(" ")} 👋",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Seu dinheiro com propósito",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SeletorMes(viewModel)
        }

        Spacer(Modifier.height(20.dp))

        CardSaldo(viewModel, mes, oculto)

        Spacer(Modifier.height(14.dp))

        viewModel.livreParaGastarHoje()?.let { livre ->
            CardLivreParaGastar(livre, oculto, onVerPlanejamento)
            Spacer(Modifier.height(14.dp))
        }

        CardInsights(viewModel.insights(mes))

        Spacer(Modifier.height(14.dp))

        CardRegra503020(viewModel, mes, oculto)

        Spacer(Modifier.height(14.dp))

        CardGastosPorCategoria(viewModel, mes, oculto)

        Spacer(Modifier.height(14.dp))

        // ---------- Últimas transações ----------
        val ultimas = viewModel.transacoesDoMes(mes).take(4)
        CardSecao(titulo = "Últimas movimentações", acao = "Ver tudo", onAcao = onVerExtrato) {
            if (ultimas.isEmpty()) {
                Text(
                    "Nada por aqui ainda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ultimas.forEach { ItemTransacao(it, oculto, mostrarData = true) }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// =====================================================================
// Card principal (saldo)
// =====================================================================

@Composable
private fun CardSaldo(viewModel: FinancasViewModel, mes: Int, oculto: Boolean) {
    val saldo = viewModel.saldoDoMes(mes)
    val entradas = viewModel.totalEntradas(mes)
    val gastos = viewModel.totalGastos(mes)
    val guardado = viewModel.totalGuardado(mes)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(GradienteInicio, GradienteFim)))
            .padding(22.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Saldo de ${nomeMes(mes).lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = viewModel::alternarOcultarValores, modifier = Modifier.size(32.dp)) {
                    Icone(
                        if (oculto) R.drawable.ic_oculto else R.drawable.ic_visivel,
                        if (oculto) "Mostrar valores" else "Esconder valores",
                        tint = Color.White.copy(alpha = 0.85f),
                        tamanho = 20.dp
                    )
                }
            }
            Text(
                text = formatarMoeda(saldo, oculto),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniResumo("Entradas", entradas, R.drawable.ic_seta_baixo, Verde80, oculto, Modifier.weight(1f))
                MiniResumo("Gastos", gastos, R.drawable.ic_seta_cima, Color(0xFFFFB4AB), oculto, Modifier.weight(1f))
            }
            if (guardado > 0) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "🐷 ${formatarMoeda(guardado, oculto)} guardados este mês",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun MiniResumo(
    rotulo: String,
    valor: Double,
    icone: Int,
    cor: Color,
    oculto: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(cor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icone(icone, null, tint = cor, tamanho = 16.dp)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(rotulo, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
            Text(
                formatarMoeda(valor, oculto),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

// =====================================================================
// Livre para gastar
// =====================================================================

@Composable
private fun CardLivreParaGastar(livre: LivreParaGastar, oculto: Boolean, onVerPlanejamento: () -> Unit) {
    val cores = FinTrackCores.atual
    val negativo = livre.porDia < 0

    CardSecao {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Livre para gastar hoje",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (negativo) "R$ 0,00" else formatarMoeda(livre.porDia, oculto),
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (negativo) cores.saida else cores.entrada
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (livre.diasRestantes == 1) "último dia" else "${livre.diasRestantes} dias restantes",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        val explicacao = if (negativo) {
            "Suas contas e metas do mês já passam do que entrou. Que tal revisar os gastos de estilo de vida?"
        } else {
            buildString {
                append("Já descontamos ")
                append(formatarMoeda(livre.contasPendentes, oculto))
                append(" de contas a vencer")
                if (livre.faltaGuardar > 0) {
                    append(" e ")
                    append(formatarMoeda(livre.faltaGuardar, oculto))
                    append(" que você planeja guardar")
                }
                append(".")
            }
        }
        Text(
            text = explicacao,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Ver contas e metas ›",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onVerPlanejamento)
                .padding(vertical = 6.dp)
        )
    }
}

// =====================================================================
// Insights
// =====================================================================

@Composable
private fun CardInsights(insights: List<Insight>) {
    if (insights.isEmpty()) return
    val cores = FinTrackCores.atual
    CardSecao(titulo = "Insights do mês") {
        insights.forEachIndexed { i, insight ->
            val cor = when (insight.tipo) {
                TipoInsight.POSITIVO -> cores.entrada
                TipoInsight.ALERTA -> cores.alerta
                TipoInsight.DICA -> MaterialTheme.colorScheme.primary
            }
            Row(verticalAlignment = Alignment.Top) {
                IconeEmoji(insight.emoji, cor, tamanho = 36.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = insight.texto,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp)
                )
            }
            if (i < insights.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}

// =====================================================================
// Regra 50/30/20
// =====================================================================

@Composable
private fun CardRegra503020(viewModel: FinancasViewModel, mes: Int, oculto: Boolean) {
    val renda = viewModel.totalEntradas(mes)
    val porGrupo = viewModel.gastosPorGrupo(mes)
    val cores = FinTrackCores.atual

    CardSecao(titulo = "Divisão da renda") {
        if (renda <= 0) {
            Text(
                "Registre uma entrada para ver como sua renda está dividida.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@CardSecao
        }
        Text(
            text = "Referência: ${viewModel.metaDoGrupo(GrupoOrcamento.ESSENCIAL)}% essenciais, " +
                "${viewModel.metaDoGrupo(GrupoOrcamento.ESTILO_DE_VIDA)}% estilo de vida e " +
                "${viewModel.metaDoGrupo(GrupoOrcamento.FUTURO)}% para o futuro.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        listOf(GrupoOrcamento.ESSENCIAL, GrupoOrcamento.ESTILO_DE_VIDA, GrupoOrcamento.FUTURO).forEach { grupo ->
            val valor = porGrupo[grupo] ?: 0.0
            val pct = (valor / renda * 100).roundToInt()
            val meta = viewModel.metaDoGrupo(grupo)
            // Para "Futuro", passar da meta é bom; para os outros, é alerta.
            val estourou = grupo != GrupoOrcamento.FUTURO && pct > meta

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(grupo.cor)
                )
                Spacer(Modifier.width(8.dp))
                Text(grupo.nome, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(
                    text = "$pct% de $meta%",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (estourou) cores.saida else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            BarraProgresso(
                progresso = pct / 100f,
                cor = if (estourou) cores.saida else grupo.cor,
                marcador = meta / 100f
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatarMoeda(valor, oculto),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

// =====================================================================
// Gastos por categoria
// =====================================================================

@Composable
private fun CardGastosPorCategoria(viewModel: FinancasViewModel, mes: Int, oculto: Boolean) {
    val porCategoria = viewModel.gastosPorCategoria(mes)
    val total = porCategoria.sumOf { it.second }
    val mesPassado = viewModel.gastosPorCategoria(mes - 1).toMap()
    val cores = FinTrackCores.atual

    CardSecao(titulo = "Para onde foi o dinheiro") {
        if (porCategoria.isEmpty()) {
            Text(
                "Nenhum gasto registrado neste mês.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@CardSecao
        }

        // Até 5 categorias + "Outras"
        val principais = porCategoria.take(5)
        val resto = porCategoria.drop(5).sumOf { it.second }
        val fatias = principais.map { it.first.cor to it.second } +
            if (resto > 0) listOf(MaterialTheme.colorScheme.outline to resto) else emptyList()

        Row(verticalAlignment = Alignment.CenterVertically) {
            GraficoRosca(
                fatias = fatias,
                modifier = Modifier.size(130.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        if (oculto) "••••" else formatarMoeda(total).replace(",00", ""),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                principais.forEach { (cat, valor) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(cat.cor))
                        Spacer(Modifier.width(6.dp))
                        Text(cat.nome, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(
                            "${(valor / total * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        principais.forEach { (cat, valor) ->
            val anterior = mesPassado[cat]
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconeCategoria(cat, tamanho = 38.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row {
                        Text(cat.nome, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        Text(formatarMoeda(valor, oculto), style = MaterialTheme.typography.titleSmall)
                    }
                    Spacer(Modifier.height(6.dp))
                    BarraProgresso(progresso = (valor / porCategoria.first().second).toFloat(), cor = cat.cor, altura = 6.dp)
                    if (anterior != null && anterior > 0) {
                        val variacao = ((valor - anterior) / anterior * 100).roundToInt()
                        if (variacao != 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (variacao > 0) "▲ $variacao% vs. mês anterior" else "▼ ${-variacao}% vs. mês anterior",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (variacao > 0) cores.saida else cores.entrada
                            )
                        }
                    }
                }
            }
        }
    }
}
