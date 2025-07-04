package com.sudokuMaster.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark

private val LightColorPalette = lightColorScheme(
    primary = primaryGreen,
    secondary = textColorLight,
    surface = lightGrey,
    inversePrimary = gridLineColorLight,
    onPrimary = accentAmber,
    onSurface = accentAmber
)

private val DarkColorPalette = darkColorScheme(
    primary = primaryCharcoal,
    secondary = textColorDark,
    surface = lightGreyAlpha,
    inversePrimary = gridLineColorLight,
    onPrimary = accentAmber,
    onSurface = accentAmber
)

@Composable
fun GraphSudokuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorPalette else LightColorPalette,
        typography = typography,
        shapes = shapes,
        content = content
    )
}