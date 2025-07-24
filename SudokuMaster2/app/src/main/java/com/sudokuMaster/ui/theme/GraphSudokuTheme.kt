package com.sudokuMaster.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import androidx.compose.ui.graphics.Color

private val LightColorPalette = lightColorScheme(
    primary = primaryGreen,
    onPrimary = Color.White,
    primaryContainer = lightGrey,
    onPrimaryContainer = primaryCharcoal,

    secondary = primaryGreen,
    onSecondary = Color.White,

    tertiary = primaryCharcoal,
    onTertiary = Color.White,

    background = Color.White,
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,

    error = Color.Red,
    onError = Color.White
)

private val DarkColorPalette = darkColorScheme(
    primary = primaryCharcoal,
    onPrimary = Color.White,
    primaryContainer = primaryCharcoal,
    onPrimaryContainer = Color.White,

    secondary = DarkGridLinePurple,
    onSecondary = Color.Black,

    tertiary = DarkGridLinePurple,
    onTertiary = Color.Black,

    background = DarkCellBackground,
    onBackground = DarkTextOnDarkBackground,

    surface = DarkCellBackground,
    onSurface = DarkTextOnDarkBackground,

    error = Color.Red,
    onError = Color.White
)

@Composable
fun GraphSudokuTheme(
    userPreferencesRepository: UserPreferencesRepositoryInterface,
    content: @Composable () -> Unit
) {
    val userPreferences by userPreferencesRepository.userPreferencesFlow.collectAsState(initial = null)

    val useDarkTheme = when (userPreferences?.appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme()
        else -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColorPalette else LightColorPalette,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
