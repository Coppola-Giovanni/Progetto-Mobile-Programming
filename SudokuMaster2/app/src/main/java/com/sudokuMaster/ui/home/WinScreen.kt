package com.sudokuMaster.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sudokuMaster.ui.activegame.ActiveGameScreenState
import com.sudokuMaster.ui.activegame.ActiveGameViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sudokuMaster.ui.Screen
import java.util.concurrent.TimeUnit

@Composable
fun WinScreen(
    navController: NavController,
    activeGameViewModel: ActiveGameViewModel // Passiamo il ViewModel per accedere allo stato
) {
    val isSolved by activeGameViewModel.isSolved.collectAsState()
    val timerState by activeGameViewModel.timerState.collectAsState()
    val isNewRecord by activeGameViewModel.isNewRecord.collectAsState() // Se decidi di implementarlo

    // Questo Composable viene mostrato solo quando il gioco è completo
    // Ma è buona pratica verificare comunque lo stato
    val activeGameScreenState by activeGameViewModel.activeGameScreenState.collectAsState()

    if (activeGameScreenState == ActiveGameScreenState.COMPLETE && isSolved) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Congratulazioni!",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Hai risolto il Sudoku!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Tempo impiegato: ${formatTime(timerState)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isNewRecord) { // Se vuoi mostrare se è un nuovo record
                Text(
                    text = "Nuovo record personale!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.popBackStack(Screen.HomeScreen.route, inclusive = false)
                    navController.navigate(Screen.HomeScreen.route) // Torna alla Home
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Torna alla Home")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Potresti voler avviare una nuova partita direttamente da qui
                    // oppure navigare a una schermata di selezione difficoltà
                    navController.popBackStack(Screen.ActiveGameScreen.route, inclusive = true) // Rimuove anche la WinScreen
                    navController.navigate(Screen.ActiveGameScreen.createRoute("new")) // Avvia una nuova partita
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Gioca una nuova partita")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate(Screen.StatisticsScreen.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vedi Statistiche")
            }
        }
    } else {
        // Se lo stato non è COMPLETE, non mostrare questa schermata o gestire un fallback
        // Questo è importante se WinScreen può essere navigato per errore.
        // In un'applicazione reale, potresti voler mostrare uno spinner o un messaggio di errore.
    }
}

fun formatTime(seconds: Long): String {
    val minutes = TimeUnit.SECONDS.toMinutes(seconds)
    val remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, remainingSeconds)
}



