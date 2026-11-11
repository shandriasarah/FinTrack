package com.example.fintrack

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InicioView(viewModel: FinancasViewModel) {
    val saldo = viewModel.calcularSaldo()
    val entradas = viewModel.calcularTotalEntradas()
    val saidas = viewModel.calcularTotalSaidas()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // --- HEADER DE SAUDAÇÃO ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Olá, Sarah!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "👋", fontSize = 18.sp)
                }
                Text(
                    text = "Veja seu resumo financeiro",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            // Ícone de Sino
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = Color(0xFFF2F2F2)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🔔", fontSize = 16.sp)
                }
            }
        }

        // --- CARD SALDO ATUAL ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Saldo atual", fontSize = 13.sp, color = Color.Gray)
                    Text(text = "👁", fontSize = 14.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "R$ %.2f".format(saldo).replace(".", ","),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "↗ 12%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006C4C)
                    )
                    Text(
                        text = " em relação ao mês passado",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- CARDS ENTRADAS E SAÍDAS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Entradas", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "R$ %.2f".format(entradas).replace(".", ","),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006C4C)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Saídas", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "R$ %.2f".format(saidas).replace(".", ","),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- VISÃO GERAL (GRÁFICO CURVO) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Visão geral", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF006C4C)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Entradas", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFD32F2F)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Saídas", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Text(text = "Este mês ∨", fontSize = 12.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    val pGreen = listOf(
                        Offset(0f, h * 0.7f),
                        Offset(w * 0.33f, h * 0.5f),
                        Offset(w * 0.66f, h * 0.15f),
                        Offset(w, h * 0.35f)
                    )

                    val pRed = listOf(
                        Offset(0f, h * 0.85f),
                        Offset(w * 0.33f, h * 0.8f),
                        Offset(w * 0.66f, h * 0.55f),
                        Offset(w, h * 0.6f)
                    )

                    // Curvas
                    val pathG = Path().apply {
                        moveTo(pGreen[0].x, pGreen[0].y)
                        pGreen.forEach { lineTo(it.x, it.y) }
                    }
                    val pathR = Path().apply {
                        moveTo(pRed[0].x, pRed[0].y)
                        pRed.forEach { lineTo(it.x, it.y) }
                    }

                    drawPath(pathG, Color(0xFF006C4C), style = Stroke(width = 5f))
                    drawPath(pathR, Color(0xFFD32F2F), style = Stroke(width = 5f))

                    // Pontos
                    pGreen.forEach { drawCircle(Color(0xFF006C4C), radius = 6f, center = it) }
                    pRed.forEach { drawCircle(Color(0xFFD32F2F), radius = 6f, center = it) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("01/05", "10/05", "20/05", "31/05").forEach {
                        Text(text = it, fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- PREVISÃO DE GASTOS ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Previsão de gastos", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Este mês", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Você tem uma previsão de gasto de", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "R$ 3.650,00",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF006C4C)
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.72f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color(0xFF006C4C),
                    trackColor = Color(0xFFEFEFEF),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "72% do orçamento previsto", fontSize = 11.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- RESUMO POR CATEGORIA ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Resumo por categoria", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Ver todas", fontSize = 12.sp, color = Color(0xFF006C4C))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(42.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("ALI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006C4C))
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Alimentação", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "R$ 1.230,00", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF006C4C))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { 0.37f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = Color(0xFF006C4C),
                            trackColor = Color(0xFFEFEFEF),
                        )
                    }
                }
            }
        }
    }
}