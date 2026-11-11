package com.example.fintrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.fintrack.ui.theme.FinTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val viewModel = ViewModelProvider(this)[FinancasViewModel::class.java]

        setContent {
            FinTrackTheme {
                TelaPrincipal(viewModel)
            }
        }
    }
}

@Composable
fun TelaPrincipal(viewModel: FinancasViewModel) {
    var telaAtual by rememberSaveable { mutableStateOf(Tela.INICIO) }
    var telaAnterior by rememberSaveable { mutableStateOf(Tela.INICIO) }

    fun irPara(tela: Tela) {
        if (tela == Tela.NOVA_TRANSACAO) telaAnterior = telaAtual
        telaAtual = tela
    }

    // Botão "voltar" do celular: fecha o formulário ou volta para o início
    BackHandler(enabled = telaAtual != Tela.INICIO) {
        telaAtual = if (telaAtual == Tela.NOVA_TRANSACAO) telaAnterior else Tela.INICIO
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (telaAtual != Tela.NOVA_TRANSACAO) {
                BarraNavegacao(telaAtual, onSelecionar = ::irPara)
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = telaAtual,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "troca_de_tela"
        ) { tela ->
            when (tela) {
                Tela.INICIO -> InicioView(
                    viewModel = viewModel,
                    onVerExtrato = { irPara(Tela.EXTRATO) },
                    onVerPlanejamento = { irPara(Tela.PLANEJAR) }
                )
                Tela.EXTRATO -> ExtratoView(viewModel)
                Tela.NOVA_TRANSACAO -> NovaTransacaoView(
                    viewModel = viewModel,
                    onFechar = { telaAtual = telaAnterior }
                )
                Tela.PLANEJAR -> PlanejarView(viewModel)
                Tela.PERFIL -> PerfilView(viewModel)
            }
        }
    }
}

@Composable
private fun BarraNavegacao(telaAtual: Tela, onSelecionar: (Tela) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Tela.entries.forEach { tela ->
            if (tela == Tela.NOVA_TRANSACAO) {
                // Botão central de adicionar
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = { onSelecionar(tela) },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icone(tela.icone, "Novo lançamento", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            } else {
                NavigationBarItem(
                    selected = telaAtual == tela,
                    onClick = { onSelecionar(tela) },
                    icon = { Icon(painterResource(tela.icone), contentDescription = null) },
                    label = { Text(tela.rotulo) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}
