package com.sudokuMaster.ui.home

import android.annotation.SuppressLint
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
import androidx.compose.ui.res.stringResource
import com.SudokuMaster.R
import com.sudokuMaster.ui.Screen
import java.util.concurrent.TimeUnit
import com.sudokuMaster.ui.userpreferences.SoundPlayer
import androidx.compose.ui.platform.LocalContext

@Composable
fun WinScreen(
    navController: NavController,
    activeGameViewModel: ActiveGameViewModel
) {
    val isSolved by activeGameViewModel.isSolved.collectAsState()
    val timerState by activeGameViewModel.timerState.collectAsState()
    val isNewRecord by activeGameViewModel.isNewRecord.collectAsState()
    val activeGameScreenState by activeGameViewModel.activeGameScreenState.collectAsState()
    val userPreferences by activeGameViewModel.userPreferencesFlow.collectAsState(initial = null)
    val soundPlayer = SoundPlayer(LocalContext.current)

    if (activeGameScreenState == ActiveGameScreenState.COMPLETE && isSolved) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.congratulazioni),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = stringResource(R.string.hai_risolto_il_sudoku),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = stringResource(R.string.tempo_impiegato, formatTime(timerState)),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (isNewRecord) {
                Text(
                    text = stringResource(R.string.nuovo_record_personale),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Ristrutturazione dei pulsanti in una Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = {
                        if (userPreferences?.soundEnabled == true) soundPlayer.playSound(R.raw.button_click_sound)
                        navController.popBackStack(Screen.HomeScreen.route, inclusive = false)
                        navController.navigate(Screen.HomeScreen.route)
                    },
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                ) {
                    Text(stringResource(R.string.back_to_home))
                }

                Button(
                    onClick = {
                        if (userPreferences?.soundEnabled == true) soundPlayer.playSound(R.raw.button_click_sound)
                        navController.popBackStack(Screen.ActiveGameScreen.route, inclusive = true)
                        navController.navigate(Screen.ActiveGameScreen.createRoute("new"))
                    },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Text(stringResource(R.string.start_new_game))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (userPreferences?.soundEnabled == true) soundPlayer.playSound(R.raw.button_click_sound)
                    navController.navigate(Screen.StatisticsScreen.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.vedi_statistiche))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Long): String {
    val minutes = TimeUnit.SECONDS.toMinutes(seconds)
    val remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

