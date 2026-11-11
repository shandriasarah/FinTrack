package com.example.fintrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsquemaClaro = lightColorScheme(
    primary = Verde40,
    onPrimary = Color.White,
    primaryContainer = Verde90,
    onPrimaryContainer = Verde10,
    secondary = Verde20,
    onSecondary = Color.White,
    secondaryContainer = Verde95,
    onSecondaryContainer = Verde20,
    tertiary = Laranja40,
    background = Cinza98,
    onBackground = Cinza10,
    surface = Color.White,
    onSurface = Cinza10,
    surfaceVariant = Cinza95,
    onSurfaceVariant = Cinza50,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Cinza95,
    outline = Cinza80,
    outlineVariant = Cinza90,
    error = Vermelho40,
    onError = Color.White
)

private val EsquemaEscuro = darkColorScheme(
    primary = Verde80,
    onPrimary = Verde10,
    primaryContainer = Verde20,
    onPrimaryContainer = Verde90,
    secondary = Verde80,
    onSecondary = Verde10,
    secondaryContainer = Cinza30,
    onSecondaryContainer = Verde90,
    tertiary = Color(0xFFFFB77C),
    background = Cinza10,
    onBackground = Cinza95,
    surface = Cinza20,
    onSurface = Cinza95,
    surfaceVariant = Cinza30,
    onSurfaceVariant = Cinza80,
    surfaceContainer = Cinza20,
    surfaceContainerHigh = Cinza30,
    outline = Cinza50,
    outlineVariant = Cinza30,
    error = Vermelho80,
    onError = Color(0xFF690005)
)

/** Cores de entrada/saída, que mudam entre tema claro e escuro. */
@Immutable
data class CoresFinanceiras(
    val entrada: Color,
    val saida: Color,
    val alerta: Color
)

private val CoresClaras = CoresFinanceiras(
    entrada = Color(0xFF00875F),
    saida = Vermelho40,
    alerta = Laranja40
)

private val CoresEscuras = CoresFinanceiras(
    entrada = Verde80,
    saida = Vermelho80,
    alerta = Color(0xFFFFB77C)
)

val LocalCoresFinanceiras = staticCompositionLocalOf { CoresClaras }

private val Formas = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun FinTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalCoresFinanceiras provides if (darkTheme) CoresEscuras else CoresClaras
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaEscuro else EsquemaClaro,
            typography = Typography,
            shapes = Formas,
            content = content
        )
    }
}

/** Atalho: `FinTrackCores.atual.entrada` */
object FinTrackCores {
    val atual: CoresFinanceiras
        @Composable get() = LocalCoresFinanceiras.current
}
