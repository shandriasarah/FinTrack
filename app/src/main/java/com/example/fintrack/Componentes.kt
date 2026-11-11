package com.example.fintrack

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fintrack.ui.theme.FinTrackCores

@Composable
fun Icone(
    @DrawableRes id: Int,
    descricao: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    tamanho: Dp = 24.dp
) {
    Icon(
        painter = painterResource(id),
        contentDescription = descricao,
        tint = tint,
        modifier = modifier.size(tamanho)
    )
}

/** Card branco padrão com título opcional e ação no canto. */
@Composable
fun CardSecao(
    modifier: Modifier = Modifier,
    titulo: String? = null,
    acao: String? = null,
    onAcao: () -> Unit = {},
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (titulo != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(titulo, style = MaterialTheme.typography.titleMedium)
                    if (acao != null) {
                        Text(
                            text = acao,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onAcao)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            conteudo()
        }
    }
}

/** "‹  Novembro  ›" */
@Composable
fun SeletorMes(viewModel: FinancasViewModel, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = viewModel::mesAnterior, modifier = Modifier.size(36.dp)) {
            Icone(R.drawable.ic_chevron_esquerda, "Mês anterior", tamanho = 20.dp)
        }
        Text(
            text = nomeMes(viewModel.mesSelecionado),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        IconButton(
            onClick = viewModel::proximoMes,
            enabled = !viewModel.ehMesAtual,
            modifier = Modifier.size(36.dp)
        ) {
            Icone(
                R.drawable.ic_chevron_direita,
                "Próximo mês",
                tamanho = 20.dp,
                tint = if (viewModel.ehMesAtual) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun IconeCategoria(categoria: Categoria, tamanho: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(RoundedCornerShape(tamanho * 0.32f))
            .background(categoria.cor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = categoria.emoji, fontSize = (tamanho.value * 0.45f).sp)
    }
}

@Composable
fun IconeEmoji(emoji: String, cor: Color, tamanho: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(tamanho)
            .clip(RoundedCornerShape(tamanho * 0.32f))
            .background(cor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = (tamanho.value * 0.45f).sp)
    }
}

@Composable
fun ItemTransacao(
    transacao: Transacao,
    oculto: Boolean,
    modifier: Modifier = Modifier,
    mostrarData: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cores = FinTrackCores.atual
    val ehEntrada = transacao.tipo == TipoTransacao.ENTRADA
    val ehGuardado = transacao.categoria == Categoria.RESERVA
    val corValor = when {
        ehEntrada -> cores.entrada
        ehGuardado -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val sinal = if (ehEntrada) "+ " else "− "

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconeCategoria(transacao.categoria)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transacao.titulo,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val subtitulo = buildString {
                append(transacao.categoria.nome)
                if (mostrarData) append(" · ").append(formatarDiaRelativo(transacao.data))
                if (transacao.contaId != null) append(" · conta fixa")
            }
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (oculto) "R$ ••••" else sinal + formatarMoeda(transacao.valor),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = corValor
        )
    }
}

/** Barra de progresso arredondada e animada. */
@Composable
fun BarraProgresso(
    progresso: Float,
    cor: Color,
    modifier: Modifier = Modifier,
    altura: Dp = 8.dp,
    marcador: Float? = null
) {
    val animado by animateFloatAsState(
        targetValue = progresso.coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "progresso"
    )
    val trilho = MaterialTheme.colorScheme.surfaceVariant
    val corMarcador = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(altura)
    ) {
        val raio = size.height / 2
        drawRoundRect(color = trilho, cornerRadius = CornerRadius(raio))
        if (animado > 0f) {
            drawRoundRect(
                color = cor,
                size = Size(width = size.width * animado, height = size.height),
                cornerRadius = CornerRadius(raio)
            )
        }
        if (marcador != null) {
            val x = size.width * marcador.coerceIn(0f, 1f)
            drawLine(
                color = corMarcador,
                start = Offset(x, -2f),
                end = Offset(x, size.height + 2f),
                strokeWidth = 3f
            )
        }
    }
}

/** Gráfico de rosca com as fatias proporcionais aos valores. */
@Composable
fun GraficoRosca(
    fatias: List<Pair<Color, Double>>,
    modifier: Modifier = Modifier,
    espessura: Dp = 18.dp,
    centro: @Composable () -> Unit = {}
) {
    val total = fatias.sumOf { it.second }
    val animacao by animateFloatAsState(
        targetValue = if (total > 0) 1f else 0f,
        animationSpec = tween(900),
        label = "rosca"
    )
    val trilho = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val traco = espessura.toPx()
            val diametro = size.minDimension - traco
            val topoEsquerdo = Offset((size.width - diametro) / 2, (size.height - diametro) / 2)
            val tamanhoArco = Size(diametro, diametro)

            drawArc(
                color = trilho,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topoEsquerdo,
                size = tamanhoArco,
                style = Stroke(width = traco)
            )

            if (total <= 0) return@Canvas
            val espaco = if (fatias.size > 1) 3f else 0f
            var inicio = -90f
            fatias.forEach { (cor, valor) ->
                val angulo = (valor / total * 360f).toFloat() * animacao
                if (angulo > espaco) {
                    drawArc(
                        color = cor,
                        startAngle = inicio + espaco / 2,
                        sweepAngle = angulo - espaco,
                        useCenter = false,
                        topLeft = topoEsquerdo,
                        size = tamanhoArco,
                        style = Stroke(width = traco, cap = StrokeCap.Butt)
                    )
                }
                inicio += angulo
            }
        }
        centro()
    }
}

@Composable
fun DialogoConfirmacao(
    titulo: String,
    mensagem: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(mensagem) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(textoConfirmar, color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
