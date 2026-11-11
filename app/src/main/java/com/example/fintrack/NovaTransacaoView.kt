package com.example.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NovaTransacaoView(
    viewModel: FinancasViewModel,
    onSalvar: () -> Unit
) {
    var descricao by remember { mutableStateOf("") }
    var valorTexto by remember { mutableStateOf("") }
    var tipoSelecionado by remember { mutableStateOf(TipoTransacao.SAIDA) }
    var categoriaSelecionada by remember { mutableStateOf(viewModel.categorias.firstOrNull() ?: Categoria("Geral", "GER", Color.Gray)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Nova Transação",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = "Preencha os dados do lançamento",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- TIPO: ENTRADA OU SAÍDA ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { tipoSelecionado = TipoTransacao.ENTRADA },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tipoSelecionado == TipoTransacao.ENTRADA) Color(0xFF006C4C) else Color(0xFFEFEFEF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Entrada",
                    color = if (tipoSelecionado == TipoTransacao.ENTRADA) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { tipoSelecionado = TipoTransacao.SAIDA },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (tipoSelecionado == TipoTransacao.SAIDA) Color(0xFFD32F2F) else Color(0xFFEFEFEF)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Saída",
                    color = if (tipoSelecionado == TipoTransacao.SAIDA) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- CAMPOS DE TEXTO ---
        OutlinedTextField(
            value = valorTexto,
            onValueChange = { valorTexto = it },
            label = { Text("Valor (R$)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição (ex: Mercado, Salário)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- SELEÇÃO DE CATEGORIA ---
        Text(
            text = "Categoria",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.categorias.forEach { cat ->
                val selecionada = categoriaSelecionada == cat
                FilterChip(
                    selected = selecionada,
                    onClick = { categoriaSelecionada = cat },
                    label = { Text(cat.nome) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = cat.cor.copy(alpha = 0.2f),
                        selectedLabelColor = cat.cor
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- BOTÃO SALVAR ---
        Button(
            onClick = {
                val valorNumerico = valorTexto.replace(",", ".").toDoubleOrNull() ?: 0.0
                if (descricao.isNotBlank() && valorNumerico > 0) {
                    viewModel.adicionarTransacao(
                        descricao = descricao,
                        valor = valorNumerico,
                        tipo = tipoSelecionado,
                        categoria = categoriaSelecionada
                    )
                    onSalvar()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006C4C)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(text = "Salvar Transação", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}