package com.example.fintrack

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PerfilView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // --- CARD DE PERFIL DO USUÁRIO ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A382C)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto / Avatar
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = CircleShape,
                    color = Color(0xFFE8F5E9)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "SO",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF006C4C)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Sarah Oliveira",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "sarah.oliveira@email.com",
                        fontSize = 13.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }

                Text(text = "›", fontSize = 22.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SEÇÃO: MINHA CONTA ---
        Text(
            text = "Minha conta",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemOpcaoMenu("Dados pessoais", "DP")
        ItemOpcaoMenu("Segurança", "SG")
        ItemOpcaoMenu("Meus cartões", "MC")
        ItemOpcaoMenu("Notificações", "NT")

        Spacer(modifier = Modifier.height(20.dp))

        // --- SEÇÃO: FINANCEIRO ---
        Text(
            text = "Financeiro",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemOpcaoMenu("Metas", "MT")
        ItemOpcaoMenu("Orçamentos", "OR")
        ItemOpcaoMenu("Relatórios", "RL")

        Spacer(modifier = Modifier.height(20.dp))

        // --- SEÇÃO: OUTROS ---
        Text(
            text = "Outros",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemOpcaoMenu("Ajuda", "AJ")
        ItemOpcaoMenu("Sobre o app", "SO")

        Spacer(modifier = Modifier.height(12.dp))

        // --- BOTÃO SAIR ---
        TextButton(
            onClick = { /* Ação de logout */ },
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Text(
                text = "Sair",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
fun ItemOpcaoMenu(titulo: String, sigla: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícone limpo usando a sigla
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF2F2F2)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = sigla,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006C4C)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            Text(text = "›", fontSize = 18.sp, color = Color.Gray)
        }
    }
}