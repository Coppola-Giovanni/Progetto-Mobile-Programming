package com.sudokuMaster.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sudokuMaster.common.ProductionDispatcherProvider
import com.sudokuMaster.common.toTime
import com.sudokuMaster.data.database.AppDatabase
import com.sudokuMaster.data.repository.GameRepositoryImpl
import com.sudokuMaster.data.repository.UserPreferencesRepositoryImpl
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.data.source.SudokuApiService
import com.sudokuMaster.data.source.ApiResponse
import com.sudokuMaster.data.userPreferencesDataStore
import com.sudokuMaster.ui.theme.GraphSudokuTheme
import java.util.concurrent.TimeUnit
import com.SudokuMaster.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController, // Now receiving NavController
    statisticsViewModel: StatisticsViewModel, // Now receiving the ViewModel directly
    modifier: Modifier = Modifier,
) {
    val userStatistics by statisticsViewModel.userStatistics.collectAsState()
    val isLoading by statisticsViewModel.isLoading.collectAsState()
    val error by statisticsViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistiche_di_gioco)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Use navController.popBackStack()
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
                Text(stringResource(R.string.caricamento_statistiche), modifier = Modifier.padding(top = 16.dp))
            } else if (error != null) {
                Text(
                    text = error ?: stringResource(R.string.errore_sconosciuto),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (userStatistics == null) {
                Text(
                    text = stringResource(R.string.nessuna_statistica_disponibile_gioca_la_tua_prima_partita),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                // Now directly access properties from UserStatisticsEntity
                userStatistics?.let { stats ->
                    Text(stringResource(R.string.statistiche_utente), style = MaterialTheme.typography.headlineLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    StatsRow(stringResource(R.string.partite_giocate_totali), stats.totalGamesPlayed.toString())
                    StatsRow(stringResource(R.string.partite_risolte_totali), stats.totalGamesSolved.toString())
                    // Use formatMillis helper function
                    StatsRow(stringResource(R.string.tempo_medio_risoluzione), formatMillis(stats.averageSolveTimeMillis))

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.migliori_tempi_per_difficolt), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Handle nullable Longs for best times
                    StatsRow(stringResource(R.string.easy), stats.bestSolveTimeEasyMillis?.let { formatMillis(it) } ?: "N/A")
                    StatsRow(stringResource(R.string.medium), stats.bestSolveTimeMediumMillis?.let { formatMillis(it) } ?: "N/A")
                    StatsRow(stringResource(R.string.hard), stats.bestSolveTimeHardMillis?.let { formatMillis(it) } ?: "N/A")
                }
            }
        }
    }
}

@Composable
fun StatsRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
    }
}

// Helper function to format milliseconds to MM:SS
fun formatMillis(millis: Long): String {
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds)
    val remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, remainingSeconds)
}