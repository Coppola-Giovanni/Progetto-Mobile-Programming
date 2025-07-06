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


private val LightColorPalette = lightColorScheme(
    primary = primaryGreen,
    secondary = textColorLight,
    surface = lightGrey,
    inversePrimary = gridLineColorLight,
    onPrimary = accentAmber,
    onSurface = DarkTextOnLightBackground
)

private val DarkColorPalette = darkColorScheme(
    primary = primaryCharcoal,
    secondary = DarkCellBackground,
    surface = DarkCellBackground,
    inversePrimary = gridLineColorLight,
    onPrimary = DarkModeInitialNumberColor,
    onSurface = DarkTextOnDarkBackground
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
