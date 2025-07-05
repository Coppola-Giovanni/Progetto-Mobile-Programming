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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sudokuMaster.common.ProductionDispatcherProvider
import com.sudokuMaster.common.toTime
import com.sudokuMaster.data.database.AppDatabase
import com.sudokuMaster.data.repository.GameRepositoryImpl
import com.sudokuMaster.data.repository.UserPreferencesRepositoryImpl
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.data.source.SudokuApiService
import com.sudokuMaster.data.source.ApiResponse
import com.sudokuMaster.data.userPreferencesDataStore
import com.sudokuMaster.domain.UserStatistics
import com.sudokuMaster.ui.theme.GraphSudokuTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Dependency Injection per ViewModel in Compose
    viewModel: StatisticsViewModel = viewModel(
        factory = StatisticsViewModel.Factory(
            gameRepository = GameRepositoryImpl(
                gameSessionDao = AppDatabase.getDatabase(androidx.compose.ui.platform.LocalContext.current).gameSessionDao(),
                userPreferencesRepository = UserPreferencesRepositoryImpl(androidx.compose.ui.platform.LocalContext.current.userPreferencesDataStore),
                // Fornisci un'implementazione dummy o reale per SudokuRemoteDataSource
                sudokuRemoteDataSource = SudokuRemoteDataSource(object : SudokuApiService {
                    override suspend fun getNewSudoku(): ApiResponse {
                        // Implementazione vuota o di test per evitare errori
                        throw UnsupportedOperationException("API service not implemented for statistics view")
                    }
                })
            ),
            dispatcherProvider = ProductionDispatcherProvider
        )
    )
) {
    val userStatistics by viewModel.userStatistics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistiche di Gioco") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
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
                Text("Caricamento statistiche...", modifier = Modifier.padding(top = 16.dp))
            } else if (error != null) {
                Text(
                    text = error ?: "Errore sconosciuto",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (userStatistics == null) {
                Text(
                    text = "Nessuna statistica disponibile. Gioca la tua prima partita!",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                userStatistics?.let { stats ->
                    StatsRow("Partite Giocate Totali:", stats.totalGamesPlayed.toString())
                    StatsRow("Partite Risolte Totali:", stats.totalGamesSolved.toString())
                    StatsRow("Tempo Medio Risoluzione:", stats.averageSolveTimeMillis?.let { (it / 1000L).toTime() } ?: "N/A")
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Migliori Tempi per Difficoltà:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    StatsRow("Facile:", stats.bestSolveTimeEasyMillis?.let { (it / 1000L).toTime() } ?: "N/A")
                    StatsRow("Medio:", stats.bestSolveTimeMediumMillis?.let { (it / 1000L).toTime() } ?: "N/A")
                    StatsRow("Difficile:", stats.bestSolveTimeHardMillis?.let { (it / 1000L).toTime() } ?: "N/A")
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
