package com.example.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FinancasView(viewModel: FinancasViewModel = remember { FinancasViewModel() }) {
    var abaSelecionada by remember { mutableStateOf("Todos") }
    var mostrarFormulario by remember { mutableStateOf(false) }

    // Estados do Formulário
    var titulo by remember { mutableStateOf("") }
    var valorText by remember { mutableStateOf("") }
    var tipoSelecionado by remember { mutableStateOf(TipoTransacao.SAIDA) }

    // Filtro dinâmico das transações
    val transacoesFiltradas = when (abaSelecionada) {
        "Entradas" -> viewModel.transacoes.filter { it.tipo == TipoTransacao.ENTRADA }
        "Saídas" -> viewModel.transacoes.filter { it.tipo == TipoTransacao.SAIDA }
        else -> viewModel.transacoes
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(16.dp)
    ) {
        // --- CABEÇALHO DO HISTÓRICO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Histórico",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { mostrarFormulario = !mostrarFormulario },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A382C))
            ) {
                Text(if (mostrarFormulario) "Fechar Form" else "+ Novo")
            }
        }

        // --- FORMULÁRIO RÁPIDO ---
        if (mostrarFormulario) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Descrição") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = valorText,
                        onValueChange = { valorText = it },
                        label = { Text("Valor (R$)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = tipoSelecionado == TipoTransacao.SAIDA,
                                onClick = { tipoSelecionado = TipoTransacao.SAIDA }
                            )
                            Text("Saída")
                            RadioButton(
                                selected = tipoSelecionado == TipoTransacao.ENTRADA,
                                onClick = { tipoSelecionado = TipoTransacao.ENTRADA }
                            )
                            Text("Entrada")
                        }
                        Button(onClick = {
                            val valor = valorText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (titulo.isNotBlank() && valor > 0.0) {
                                viewModel.adicionarTransacao(
                                    titulo = titulo.trim(),
                                    valor = valor,
                                    tipo = tipoSelecionado,
                                    categoria = if (tipoSelecionado == TipoTransacao.ENTRADA) Categoria.SALARIO.nome else Categoria.MERCADO.nome
                                )
                                titulo = ""
                                valorText = ""
                                mostrarFormulario = false
                            }
                        }) {
                            Text("Salvar")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- ABAS DE FILTRO (Todos | Entradas | Saídas) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEEEEEE))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Todos", "Entradas", "Saídas").forEach { aba ->
                val selecionada = abaSelecionada == aba
                Button(
                    onClick = { abaSelecionada = aba },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selecionada) Color.White else Color.Transparent,
                        contentColor = if (selecionada) Color.Black else Color.Gray
                    ),
                    elevation = null,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = aba, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- LISTA COM DESIGN ATUALIZADO ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(transacoesFiltradas) { item ->
                val ehEntrada = item.tipo == TipoTransacao.ENTRADA
                val corValor = if (ehEntrada) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                val sinal = if (ehEntrada) "+" else "-"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Ícone Arredondado
                        // Ícone Arredondado com a Sigla em Texto
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(item.categoria.cor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.categoria.sigla,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = item.categoria.cor
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Título e Nome da Categoria
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.titulo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = item.categoria.nome,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }

                        // Valor e Ação de Excluir
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "R$ $sinal%.2f".format(item.valor),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = corValor
                            )
                            TextButton(
                                onClick = { viewModel.removerTransacao(item) },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(24.dp)
                            ) {
                                Text("excluir", color = Color.LightGray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}