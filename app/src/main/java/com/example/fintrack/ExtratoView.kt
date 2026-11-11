package com.example.fintrack

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fintrack.ui.theme.FinTrackCores

private enum class FiltroExtrato(val rotulo: String) {
    TODOS("Todos"),
    ENTRADAS("Entradas"),
    GASTOS("Gastos"),
    GUARDADO("Guardado")
}

@Composable
fun ExtratoView(viewModel: FinancasViewModel) {
    var busca by rememberSaveable { mutableStateOf("") }
    var filtro by rememberSaveable { mutableStateOf(FiltroExtrato.TODOS) }
    var paraExcluir by remember { mutableStateOf<Transacao?>(null) }
    val oculto = viewModel.ocultarValores
    val cores = FinTrackCores.atual

    val lista = viewModel.transacoesDoMes()
        .filter {
            when (filtro) {
                FiltroExtrato.TODOS -> true
                FiltroExtrato.ENTRADAS -> it.tipo == TipoTransacao.ENTRADA
                FiltroExtrato.GASTOS -> it.tipo == TipoTransacao.SAIDA && it.categoria != Categoria.RESERVA
                FiltroExtrato.GUARDADO -> it.categoria == Categoria.RESERVA
            }
        }
        .filter {
            busca.isBlank() ||
                it.titulo.contains(busca, ignoreCase = true) ||
                it.categoria.nome.contains(busca, ignoreCase = true)
        }

    val porDia = lista.groupBy { inicioDoDia(it.data) }.toSortedMap(compareByDescending { it })

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Extrato", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                SeletorMes(viewModel)
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = busca,
                onValueChange = { busca = it },
                placeholder = { Text("Buscar lançamento ou categoria") },
                leadingIcon = { Icone(R.drawable.ic_busca, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (busca.isNotEmpty()) {
                        IconButton(onClick = { busca = "" }) {
                            Icone(R.drawable.ic_fechar, "Limpar busca", tamanho = 20.dp)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroExtrato.entries.forEach { f ->
                    FilterChip(
                        selected = filtro == f,
                        onClick = { filtro = f },
                        label = { Text(f.rotulo) }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            if (lista.isNotEmpty()) {
                val entradas = lista.filter { it.tipo == TipoTransacao.ENTRADA }.sumOf { it.valor }
                val saidas = lista.filter { it.tipo == TipoTransacao.SAIDA }.sumOf { it.valor }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${lista.size} lançamento${if (lista.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    if (entradas > 0) {
                        Text("+ ${formatarMoeda(entradas, oculto)}", style = MaterialTheme.typography.labelLarge, color = cores.entrada)
                        Spacer(Modifier.padding(horizontal = 6.dp))
                    }
                    if (saidas > 0) {
                        Text("− ${formatarMoeda(saidas, oculto)}", style = MaterialTheme.typography.labelLarge, color = cores.saida)
                    }
                }
            }
        }

        if (lista.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔍", style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (busca.isNotBlank()) "Nada encontrado para \"$busca\"" else "Nenhum lançamento neste mês",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        porDia.forEach { (dia, transacoesDoDia) ->
            item(key = "dia_$dia") {
                val saldoDia = transacoesDoDia.sumOf { if (it.tipo == TipoTransacao.ENTRADA) it.valor else -it.valor }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatarDiaRelativo(dia).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        (if (saldoDia >= 0) "+ " else "− ") + formatarMoeda(kotlin.math.abs(saldoDia), oculto),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(transacoesDoDia, key = { it.id }) { t ->
                ItemTransacao(t, oculto, onClick = { paraExcluir = t })
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            if (lista.isNotEmpty()) {
                Text(
                    "Toque em um lançamento para excluir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    paraExcluir?.let { t ->
        DialogoConfirmacao(
            titulo = "Excluir lançamento?",
            mensagem = "\"${t.titulo}\" de ${formatarMoeda(t.valor)} será removido." +
                if (t.contaId != null) " A conta fixa voltará a aparecer como pendente." else "",
            textoConfirmar = "Excluir",
            onConfirmar = {
                viewModel.removerTransacao(t)
                paraExcluir = null
            },
            onCancelar = { paraExcluir = null }
        )
    }
}
