package com.example.fintrack

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MetasView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // --- HEADER ---
        Text(
            text = "Pendências",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A)
        )
        Text(
            text = "Suas contas e assinaturas do mês",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- CARD RESUMO TOTAL ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A382C)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Total a pagar", fontSize = 14.sp, color = Color(0xFFB0BEC5))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "R$ 450,00",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Vencem hoje",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ItemPendencia("Cartão de Crédito", "CC", "Vence hoje", 250.00, Color(0xFFD32F2F))
        ItemPendencia("Plano de Saúde", "PS", "Vence hoje", 199.90, Color(0xFF1976D2))

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Próximos vencimentos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        ItemPendencia("Netflix", "NF", "Vence em 2 dias", 39.90, Color(0xFFD32F2F))
        ItemPendencia("Spotify", "SP", "Vence em 5 dias", 21.90, Color(0xFF006C4C))
        ItemPendencia("Conta de Luz", "LZ", "Vence em 10 dias", 185.50, Color(0xFFF57C00))
        ItemPendencia("Internet", "IN", "Vence em 12 dias", 120.00, Color(0xFF1976D2))
        ItemPendencia("Academia", "AC", "Vence em 15 dias", 82.70, Color(0xFF8E24AA))

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun ItemPendencia(titulo: String, sigla: String, prazo: String, valor: Double, corIcone: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone com sigla
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(12.dp),
                color = corIcone.copy(alpha = 0.1f) // Fundo transparente baseado na cor original
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = sigla,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = corIcone
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Textos (Título e Prazo)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Text(
                    text = prazo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            // Valor e Botão Pagar
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "R$ %.2f".format(valor).replace(".", ","),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF006C4C),
                    onClick = { /* Ação de pagar conta */ }
                ) {
                    Text(
                        text = "Pagar",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}