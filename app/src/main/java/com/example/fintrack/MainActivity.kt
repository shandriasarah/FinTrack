package com.example.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}
when (telaAtual) {
    Tela.INICIO -> InicioView(viewModel = viewModel)
    Tela.HISTORICO -> FinancasView(viewModel = viewModel)
    Tela.NOVA_TRANSACAO -> NovaTransacaoView(
    viewModel = viewModel,
    onSalvar = { telaAtual = Tela.HISTORICO }
    )
    Tela.METAS -> MetasView()
    Tela.PERFIL -> PerfilView()
}

@Composable
fun MainScreen() {
    var telaAtual by remember { mutableStateOf(Tela.INICIO) }
    val viewModel: FinancasViewModel = remember { FinancasViewModel() }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Início
                    TextButton(onClick = { telaAtual = Tela.INICIO }) {
                        Text(
                            text = "Início",
                            color = if (telaAtual == Tela.INICIO) Color(0xFF006C4C) else Color.Gray
                        )
                    }

                    // Botão Histórico
                    TextButton(onClick = { telaAtual = Tela.HISTORICO }) {
                        Text(
                            text = "Histórico",
                            color = if (telaAtual == Tela.HISTORICO) Color(0xFF006C4C) else Color.Gray
                        )
                    }

                    // Botão Central (+)
                    FloatingActionButton(
                        onClick = { telaAtual = Tela.NOVA_TRANSACAO },
                        shape = CircleShape,
                        containerColor = Color(0xFF0A382C),
                        contentColor = Color.White,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text("+", style = MaterialTheme.typography.titleLarge)
                    }

                    // Botão Metas
                    TextButton(onClick = { telaAtual = Tela.METAS }) {
                        Text(
                            text = "Metas",
                            color = if (telaAtual == Tela.METAS) Color(0xFF006C4C) else Color.Gray
                        )
                    }

                    // Botão Perfil
                    TextButton(onClick = { telaAtual = Tela.PERFIL }) {
                        Text(
                            text = "Perfil",
                            color = if (telaAtual == Tela.PERFIL) Color(0xFF006C4C) else Color.Gray
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        when (telaAtual) {
            Tela.INICIO -> InicioView(viewModel = viewModel)
            Tela.HISTORICO -> FinancasView(viewModel = viewModel)
            Tela.NOVA_TRANSACAO -> FinancasView(viewModel = viewModel)
            Tela.METAS -> MetasView()
            Tela.PERFIL -> PerfilView() // Chamando a nova tela construída!
        }
    }
}
