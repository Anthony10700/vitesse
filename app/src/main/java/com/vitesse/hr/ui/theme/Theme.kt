package com.vitesse.hr.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// bleu = primary (top bar, boutons)c
// rose = secondary (favoris, accents)
// jaune = tertiary (petits accents)
private val LightColors = lightColorScheme(
    primary = VitesseBlue,
    secondary = VitesseRose,
    tertiary = VitesseYellow
)

private val DarkColors = darkColorScheme(
    primary = VitesseBlue,
    secondary = VitesseRose,
    tertiary = VitesseYellow
)

// un wrapper autour de chaque écran et de chaque @Preview
@Composable
fun VitesseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
