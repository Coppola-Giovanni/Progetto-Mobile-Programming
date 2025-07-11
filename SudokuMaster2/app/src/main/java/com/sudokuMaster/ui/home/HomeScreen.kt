package com.sudokuMaster.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.SudokuMaster.R
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale // Importa ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sudokuMaster.ui.theme.isDark
import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.UserPreferences
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun HomeScreen(
    onNewGameClick: () -> Unit,
    onContinueGameClick: () -> Unit,
    onViewStatisticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentThemeDark = MaterialTheme.colorScheme.isDark()

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(
                id = if (isCurrentThemeDark) R.drawable.dark_mode_background else R.drawable.light_mode_background
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sudoku Master",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 60.sp,              // Più grande
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Cursive, // Font "rotondo" (Cursive è un esempio, valuta di aggiungere un font personalizzato se vuoi uno specifico)
                    color = if (isCurrentThemeDark) Color.White else Color.Black
                ),
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Per tutti i pulsanti:
            Button(
                onClick = onNewGameClick,
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                Text(
                    text = stringResource(R.string.nuova_partita),
                    color = Color.White, // Testo bianco
                    fontSize = 20.sp     // Più grande
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueGameClick,
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                Text(
                    text = stringResource(R.string.continua_partita),
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onViewStatisticsClick,
                modifier = Modifier.widthIn(min = 200.dp)
            ) {
                Text(
                    text = stringResource(R.string.statistiche_di_gioco),
                    color = Color.White,
                    fontSize = 20.sp
                )
            }
        }
    }
}
