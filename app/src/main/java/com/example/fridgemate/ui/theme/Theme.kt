package com.example.fridgemate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = DarkGreen,
    tertiary = LightGreen,
    background = BackgroundWhite,
    surface = CardBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun FRIDGEMATETheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
