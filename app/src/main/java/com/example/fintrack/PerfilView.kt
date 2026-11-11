package com.example.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.fintrack.ui.theme.GradienteFim
import com.example.fintrack.ui.theme.GradienteInicio
import kotlin.math.roundToInt

@Composable
fun PerfilView(viewModel: FinancasViewModel) {
    var editarNome by remember { mutableStateOf(false) }
    var confirmarApagar by remember { mutableStateOf(false) }
    var confirmarExemplo by remember { mutableStateOf(false) }
    var mostrarSobre by remember { mutableStateOf(false) }
    var reserva by remember { mutableFloatStateOf(viewModel.percentualReserva.toFloat()) }

    val totalLancamentos = viewModel.transacoes.size
    val totalGuardado = viewModel.metas.sumOf { viewModel.guardadoNaMeta(it) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Perfil", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        // ---------- Cartão do usuário ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(GradienteInicio, GradienteFim)))
                .clickable { editarNome = true }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciais(viewModel.nomeUsuario), style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(viewModel.nomeUsuario, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(
                    "$totalLancamentos lançamentos · ${formatarMoeda(totalGuardado, viewModel.ocultarValores)} em metas",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
            Text("Editar", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
        }

        Spacer(Modifier.height(14.dp))

        // ---------- Planejamento ----------
        CardSecao(titulo = "Seu plano") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Guardar por mês", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(
                    "${reserva.roundToInt()}% da renda",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = reserva,
                onValueChange = { reserva = it },
                onValueChangeFinished = { viewModel.atualizarPercentualReserva(reserva.roundToInt()) },
                valueRange = 0f..40f,
                steps = 7
            )
            Text(
                "Usado no \"livre para gastar\" e na divisão 50/30/20. Especialistas costumam recomendar guardar ao menos 10–20% da renda.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Esconder valores", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Útil para abrir o app em público",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = viewModel.ocultarValores, onCheckedChange = { viewModel.alternarOcultarValores() })
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---------- Dados ----------
        CardSecao(titulo = "Dados") {
            ItemMenu("🧪", "Carregar dados de exemplo", "Substitui tudo por um mês fictício") { confirmarExemplo = true }
            ItemMenu("🗑️", "Apagar todos os dados", "Começar do zero", destrutivo = true) { confirmarApagar = true }
            ItemMenu("ℹ️", "Sobre o FinTrack", "Versão 2.0") { mostrarSobre = true }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Seus dados ficam salvos apenas neste celular.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
    }

    if (editarNome) {
        var nome by remember { mutableStateOf(viewModel.nomeUsuario) }
        AlertDialog(
            onDismissRequest = { editarNome = false },
            title = { Text("Como podemos te chamar?") },
            text = {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it.take(30) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.atualizarNome(nome)
                    editarNome = false
                }) { Text("Salvar") }
            },
            dismissButton = {
                TextButton(onClick = { editarNome = false }) { Text("Cancelar") }
            }
        )
    }

    if (confirmarApagar) {
        DialogoConfirmacao(
            titulo = "Apagar tudo?",
            mensagem = "Todos os lançamentos, contas fixas e metas serão apagados. Isso não pode ser desfeito.",
            textoConfirmar = "Apagar",
            onConfirmar = {
                viewModel.apagarTudo()
                confirmarApagar = false
            },
            onCancelar = { confirmarApagar = false }
        )
    }

    if (confirmarExemplo) {
        DialogoConfirmacao(
            titulo = "Carregar exemplo?",
            mensagem = "Seus dados atuais serão substituídos por dados fictícios para demonstração.",
            textoConfirmar = "Carregar",
            onConfirmar = {
                viewModel.carregarDadosDeExemplo()
                confirmarExemplo = false
            },
            onCancelar = { confirmarExemplo = false }
        )
    }

    if (mostrarSobre) {
        AlertDialog(
            onDismissRequest = { mostrarSobre = false },
            title = { Text("FinTrack") },
            text = {
                Text(
                    "Um app para saber, todo dia, quanto você pode gastar sem comprometer suas contas e seus objetivos.\n\n" +
                        "• Livre para gastar: renda − gastos − contas a vencer − quanto você quer guardar, dividido pelos dias que faltam.\n" +
                        "• Regra 50/30/20: essenciais, estilo de vida e futuro."
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarSobre = false }) { Text("Fechar") }
            }
        )
    }
}

@Composable
private fun ItemMenu(
    emoji: String,
    titulo: String,
    subtitulo: String,
    destrutivo: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconeEmoji(
            emoji,
            if (destrutivo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            tamanho = 38.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                titulo,
                style = MaterialTheme.typography.titleSmall,
                color = if (destrutivo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icone(R.drawable.ic_chevron_direita, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, tamanho = 20.dp)
    }
}
