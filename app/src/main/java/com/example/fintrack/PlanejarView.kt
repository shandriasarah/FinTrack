package com.example.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fintrack.ui.theme.FinTrackCores
import kotlin.math.roundToInt

@Composable
fun PlanejarView(viewModel: FinancasViewModel) {
    val oculto = viewModel.ocultarValores
    val mes = viewModel.mesSelecionado
    val cores = FinTrackCores.atual

    var mostrarNovaConta by remember { mutableStateOf(false) }
    var mostrarNovaMeta by remember { mutableStateOf(false) }
    var metaParaGuardar by remember { mutableStateOf<Meta?>(null) }
    var contaParaExcluir by remember { mutableStateOf<ContaFixa?>(null) }
    var metaParaExcluir by remember { mutableStateOf<Meta?>(null) }

    val pendentes = viewModel.contasPendentes(mes)
    val totalPendente = pendentes.sumOf { it.valor }
    val totalContas = viewModel.contas.sumOf { it.valor }
    val pagas = viewModel.contas.size - pendentes.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Planejar", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Contas do mês e objetivos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SeletorMes(viewModel)
        }

        Spacer(Modifier.height(20.dp))

        // ---------- Resumo das contas ----------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.secondary)
                .padding(20.dp)
        ) {
            Column {
                Text("Falta pagar em ${nomeMes(mes).lowercase()}", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodyMedium)
                Text(formatarMoeda(totalPendente, oculto), color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))
                BarraProgresso(
                    progresso = if (viewModel.contas.isEmpty()) 0f else pagas.toFloat() / viewModel.contas.size,
                    cor = Color(0xFF6CDBAC),
                    altura = 6.dp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "$pagas de ${viewModel.contas.size} contas pagas · ${formatarMoeda(totalContas, oculto)} em contas fixas",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- Contas fixas ----------
        CardSecao(titulo = "Contas fixas", acao = "+ Nova", onAcao = { mostrarNovaConta = true }) {
            if (viewModel.contas.isEmpty()) {
                Text(
                    "Cadastre aluguel, internet, assinaturas... Elas entram no cálculo do \"livre para gastar\".",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val hoje = diaDoMes(System.currentTimeMillis())
            viewModel.contas.sortedWith(compareBy<ContaFixa>({ viewModel.contaPaga(it, mes) }, { it.diaVencimento })).forEach { conta ->
                val paga = viewModel.contaPaga(conta, mes)
                val atrasada = !paga && viewModel.ehMesAtual && conta.diaVencimento < hoje
                val prazo = when {
                    paga -> "Paga ✓"
                    !viewModel.ehMesAtual -> "Vencia dia ${conta.diaVencimento}"
                    conta.diaVencimento == hoje -> "Vence hoje"
                    conta.diaVencimento == hoje + 1 -> "Vence amanhã"
                    atrasada -> "Atrasada · venceu dia ${conta.diaVencimento}"
                    else -> "Vence dia ${conta.diaVencimento}"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { contaParaExcluir = conta }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconeCategoria(conta.categoria)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(conta.nome, style = MaterialTheme.typography.titleSmall)
                        Text(
                            prazo,
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                paga -> cores.entrada
                                atrasada -> cores.saida
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    Text(formatarMoeda(conta.valor, oculto), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(10.dp))
                    BotaoPagar(paga = paga, onClick = { viewModel.alternarPagamento(conta) })
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- Metas ----------
        CardSecao(titulo = "Metas de economia", acao = "+ Nova", onAcao = { mostrarNovaMeta = true }) {
            if (viewModel.metas.isEmpty()) {
                Text(
                    "Crie uma meta (viagem, reserva de emergência, carro...) e vá guardando aos poucos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            viewModel.metas.forEachIndexed { i, meta ->
                val guardado = viewModel.guardadoNaMeta(meta)
                val progresso = if (meta.valorAlvo > 0) (guardado / meta.valorAlvo).toFloat() else 0f
                val concluida = progresso >= 1f

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconeEmoji(meta.emoji, MaterialTheme.colorScheme.primary, tamanho = 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(meta.nome, style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (concluida) "Meta alcançada! 🎉" else "Faltam ${formatarMoeda(meta.valorAlvo - guardado, oculto)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (concluida) cores.entrada else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { metaParaExcluir = meta }, modifier = Modifier.size(32.dp)) {
                            Icone(R.drawable.ic_excluir, "Excluir meta", tint = MaterialTheme.colorScheme.onSurfaceVariant, tamanho = 18.dp)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    BarraProgresso(progresso = progresso, cor = if (concluida) cores.entrada else MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${formatarMoeda(guardado, oculto)} de ${formatarMoeda(meta.valorAlvo, oculto)} · ${(progresso * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (!concluida) {
                            TextButton(onClick = { metaParaGuardar = meta }) {
                                Text("Guardar")
                            }
                        }
                    }
                }
                if (i < viewModel.metas.lastIndex) Spacer(Modifier.height(10.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Dica: toque em uma conta para removê-la. O valor guardado nas metas aparece no extrato como \"Guardado\".",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
    }

    // ---------- Diálogos ----------

    if (mostrarNovaConta) {
        DialogoNovaConta(
            onSalvar = { nome, valor, dia, categoria ->
                viewModel.adicionarConta(nome, valor, dia, categoria)
                mostrarNovaConta = false
            },
            onCancelar = { mostrarNovaConta = false }
        )
    }

    if (mostrarNovaMeta) {
        DialogoNovaMeta(
            onSalvar = { nome, emoji, alvo ->
                viewModel.adicionarMeta(nome, emoji, alvo)
                mostrarNovaMeta = false
            },
            onCancelar = { mostrarNovaMeta = false }
        )
    }

    metaParaGuardar?.let { meta ->
        DialogoValor(
            titulo = "Guardar em \"${meta.nome}\"",
            descricao = "O valor sai do saldo do mês e vai para a meta.",
            onSalvar = { valor ->
                viewModel.guardarNaMeta(meta, valor)
                metaParaGuardar = null
            },
            onCancelar = { metaParaGuardar = null }
        )
    }

    contaParaExcluir?.let { conta ->
        DialogoConfirmacao(
            titulo = "Remover conta fixa?",
            mensagem = "\"${conta.nome}\" deixará de aparecer nos próximos meses. Pagamentos já registrados continuam no extrato.",
            textoConfirmar = "Remover",
            onConfirmar = {
                viewModel.removerConta(conta)
                contaParaExcluir = null
            },
            onCancelar = { contaParaExcluir = null }
        )
    }

    metaParaExcluir?.let { meta ->
        DialogoConfirmacao(
            titulo = "Excluir meta?",
            mensagem = "A meta \"${meta.nome}\" será removida. Os valores guardados continuam no extrato.",
            textoConfirmar = "Excluir",
            onConfirmar = {
                viewModel.removerMeta(meta)
                metaParaExcluir = null
            },
            onCancelar = { metaParaExcluir = null }
        )
    }
}

@Composable
private fun BotaoPagar(paga: Boolean, onClick: () -> Unit) {
    val cores = FinTrackCores.atual
    if (paga) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(cores.entrada.copy(alpha = 0.15f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icone(R.drawable.ic_check, "Desfazer pagamento", tint = cores.entrada, tamanho = 20.dp)
        }
    } else {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Pagar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun DialogoNovaConta(
    onSalvar: (String, Double, Int, Categoria) -> Unit,
    onCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var valorTexto by remember { mutableStateOf("") }
    var diaTexto by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(Categoria.CONTAS) }

    val valor = converterValor(valorTexto)
    val dia = diaTexto.toIntOrNull()
    val valido = nome.isNotBlank() && valor != null && valor > 0 && dia != null && dia in 1..31

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nova conta fixa") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it.take(30) },
                    label = { Text("Nome (ex: Internet)") },
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = TextFieldValue(valorTexto, TextRange(valorTexto.length)),
                        onValueChange = { valorTexto = mascaraMoeda(it.text) },
                    prefix = { if (valorTexto.isNotEmpty()) Text("R$ ") },
                    placeholder = { Text("0,00") },
                        label = { Text("Valor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = diaTexto,
                        onValueChange = { diaTexto = it.filter(Char::isDigit).take(2) },
                        label = { Text("Dia") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Categoria.doTipo(TipoTransacao.SAIDA).forEach { cat ->
                        FilterChip(
                            selected = categoria == cat,
                            onClick = { categoria = cat },
                            label = { Text("${cat.emoji} ${cat.nome}") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSalvar(nome, valor ?: 0.0, dia ?: 1, categoria) }, enabled = valido) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

private val emojisMeta = listOf("🛟", "✈️", "🏠", "🚗", "🎓", "💍", "💻", "🎁")

@Composable
private fun DialogoNovaMeta(
    onSalvar: (String, String, Double) -> Unit,
    onCancelar: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var valorTexto by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(emojisMeta.first()) }

    val valor = converterValor(valorTexto)
    val valido = nome.isNotBlank() && valor != null && valor > 0

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Nova meta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    emojisMeta.forEach { e ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (e == emoji) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    else Modifier
                                )
                                .clickable { emoji = e },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(e, fontSize = 18.sp)
                        }
                    }
                }
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it.take(30) },
                    label = { Text("Nome (ex: Viagem)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = TextFieldValue(valorTexto, TextRange(valorTexto.length)),
                    onValueChange = { valorTexto = mascaraMoeda(it.text) },
                    prefix = { if (valorTexto.isNotEmpty()) Text("R$ ") },
                    placeholder = { Text("0,00") },
                    label = { Text("Quanto quer juntar (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSalvar(nome, emoji, valor ?: 0.0) }, enabled = valido) {
                Text("Criar meta")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}

@Composable
fun DialogoValor(
    titulo: String,
    descricao: String,
    onSalvar: (Double) -> Unit,
    onCancelar: () -> Unit
) {
    var valorTexto by remember { mutableStateOf("") }
    val valor = converterValor(valorTexto)

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(descricao, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = TextFieldValue(valorTexto, TextRange(valorTexto.length)),
                    onValueChange = { valorTexto = mascaraMoeda(it.text) },
                    prefix = { if (valorTexto.isNotEmpty()) Text("R$ ") },
                    placeholder = { Text("0,00") },
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { valor?.let(onSalvar) }, enabled = valor != null && valor > 0) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
