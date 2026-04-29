package com.example.bts_task.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = Color.White,
    secondary = BrandGreen,
    onSecondary = Color.White,
    tertiary = BrandGreen,
    background = SurfaceGrey,
    onBackground = TextDark,
    surface = Color.White,
    onSurface = TextDark,
    onSurfaceVariant = TextGrey,
    outline = OutlineGrey
)

@Composable
fun BtstaskTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
