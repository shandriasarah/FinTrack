package com.example.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fintrack.ui.theme.FinTrackCores

@Composable
fun NovaTransacaoView(
    viewModel: FinancasViewModel,
    onFechar: () -> Unit
) {
    var tipo by rememberSaveable { mutableStateOf(TipoTransacao.SAIDA) }
    var valorTexto by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf(Categoria.MERCADO) }
    var foiOntem by rememberSaveable { mutableStateOf(false) }

    val cores = FinTrackCores.atual
    val corTipo = if (tipo == TipoTransacao.ENTRADA) cores.entrada else cores.saida
    val valor = converterValor(valorTexto)
    val valido = valor != null && valor > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Novo lançamento", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onFechar) {
                Icone(R.drawable.ic_fechar, "Fechar")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---------- Tipo ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            listOf(TipoTransacao.SAIDA to "Gasto", TipoTransacao.ENTRADA to "Entrada").forEach { (t, rotulo) ->
                val selecionado = tipo == t
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selecionado) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            if (tipo != t) {
                                tipo = t
                                categoria = Categoria.doTipo(t).first()
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        rotulo,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selecionado) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // ---------- Valor ----------
        Text(
            "Valor",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        OutlinedTextField(
            // Cursor sempre no fim: cada dígito entra pela direita (centavos).
            value = TextFieldValue(valorTexto, TextRange(valorTexto.length)),
            onValueChange = { novo -> valorTexto = mascaraMoeda(novo.text) },
            placeholder = {
                Text(
                    "R$ 0,00",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            prefix = { if (valorTexto.isNotEmpty()) Text("R$ ", style = MaterialTheme.typography.displaySmall, color = corTipo) },
            textStyle = MaterialTheme.typography.displaySmall.copy(color = corTipo, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )

        Spacer(Modifier.height(16.dp))

        // ---------- Descrição ----------
        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it.take(40) },
            label = { Text("Descrição (opcional)") },
            placeholder = { Text(categoria.nome) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        // ---------- Categoria ----------
        Text("Categoria", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(10.dp))
        Categoria.doTipo(tipo).chunked(4).forEach { linha ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                linha.forEach { cat ->
                    val selecionada = categoria == cat
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (selecionada) {
                                    Modifier
                                        .background(cat.cor.copy(alpha = 0.12f))
                                        .border(1.5.dp, cat.cor, RoundedCornerShape(16.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { categoria = cat }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(cat.emoji, fontSize = 24.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            cat.nome,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selecionada) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                // completa a linha para manter o alinhamento
                repeat(4 - linha.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(12.dp))

        // ---------- Data ----------
        Text("Quando", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !foiOntem, onClick = { foiOntem = false }, label = { Text("Hoje") })
            FilterChip(selected = foiOntem, onClick = { foiOntem = true }, label = { Text("Ontem") })
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                val v = valor ?: return@Button
                viewModel.adicionarTransacao(
                    titulo = descricao.ifBlank { categoria.nome },
                    valor = v,
                    tipo = tipo,
                    categoria = categoria,
                    data = if (foiOntem) ontem() else System.currentTimeMillis()
                )
                onFechar()
            },
            enabled = valido,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                if (tipo == TipoTransacao.ENTRADA) "Adicionar entrada" else "Adicionar gasto",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}
