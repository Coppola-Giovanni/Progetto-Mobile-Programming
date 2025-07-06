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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface // Importa questo


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
    // Rimuoviamo il parametro darkTheme predefinito da isSystemInDarkTheme()
    // Ora il repository viene iniettato qui
    userPreferencesRepository: UserPreferencesRepositoryInterface, // <-- NUOVO PARAMETRO
    content: @Composable () -> Unit
) {
    // Rimuovi: val userPreferencesRepository: UserPreferencesRepositoryInterface = koinInject()

    // Raccogli il flusso delle preferenze utente
    val userPreferences by userPreferencesRepository.userPreferencesFlow.collectAsState(initial = null)

    // Determina il tema scuro in base alle preferenze dell'utente
    val useDarkTheme = when (userPreferences?.appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM_DEFAULT -> isSystemInDarkTheme() // Usa il tema di sistema se è l'opzione scelta
        else -> isSystemInDarkTheme() // Fallback se userPreferences è null o non definito
    }

    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColorPalette else LightColorPalette,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
