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
import com.sudokuMaster.ui.theme.DarkCellBackground
import com.sudokuMaster.ui.theme.DarkGridLinePurple
import com.sudokuMaster.ui.theme.DarkTextOnDarkBackground
import com.sudokuMaster.ui.theme.lightGrey
import com.sudokuMaster.ui.theme.primaryCharcoal
import com.sudokuMaster.ui.theme.primaryGreen

private val LightColorPalette = lightColorScheme(
    primary = primaryGreen,               // Colore principale (per i pulsanti New Game, Continue, Stats)
    onPrimary = Color.White,              // Testo/icone su primary (es. testo sui pulsanti Home Screen)
    primaryContainer = lightGrey,         // Contenitore primario (es. sfondo TopAppBar in Light Mode)
    onPrimaryContainer = primaryCharcoal, // Testo/icone su primaryContainer (es. titolo TopAppBar in Light Mode)

    secondary = primaryGreen,             // Colore di accento secondario (es. per i numeri readOnly, se gestito qui, o per Switch)
    onSecondary = Color.White,            // Testo/icone su secondary

    tertiary = primaryCharcoal,           // Un colore aggiuntivo, se usato specificamente (es. pulsante Notes ON/OFF)
    onTertiary = Color.White,             // Testo/icone su tertiary

    background = Color.White,             // Sfondo generale della schermata (sotto lo sfondo immagine)
    onBackground = Color.Black,           // Testo/icone su background

    surface = Color.White,                // Superficie (es. sfondi di card, celle della griglia)
    onSurface = Color.Black,              // Testo/icone su surface (es. testo generico, numeri inseriti)

    // Aggiungi colori per i vari stati se necessario, ad es. per gli errori
    error = Color.Red,
    onError = Color.White
)

private val DarkColorPalette = darkColorScheme(
    primary = primaryCharcoal,            // Colore principale per Dark Mode
    onPrimary = Color.White,              // Testo/icone su primary per Dark Mode
    primaryContainer = primaryCharcoal,
    onPrimaryContainer = Color.White,

    secondary = DarkGridLinePurple, // Ora secondary è il colore per i numeri iniziali nel dark
    onSecondary = Color.Black,

    tertiary = DarkGridLinePurple, // O un altro colore se vuoi un contrasto diverso
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
