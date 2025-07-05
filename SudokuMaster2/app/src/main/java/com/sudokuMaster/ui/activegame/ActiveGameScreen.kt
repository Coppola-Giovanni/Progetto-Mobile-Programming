package com.sudokuMaster.ui.activegame

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel // Importa questa funzione
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.domain.getHash
import java.util.concurrent.TimeUnit


@Composable
fun ActiveGameScreen(
    viewModelFactory: ActiveGameViewModelFactory,
    onGameSolved: () -> Unit,  //Callback per gioco risolto
    onGameLoadError: () -> Unit,  //Callback per errore di caricamento
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActiveGameViewModel = viewModel(
        factory = viewModelFactory)

    // Osserva gli StateFlow del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val sudokuGrid by viewModel.sudokuGrid.collectAsState()
    val selectedTile by viewModel.selectedTile.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()
    val showLoading by viewModel.showLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isNewRecord by viewModel.isNewRecord.collectAsState()

    // Gestione degli effetti collaterali
    LaunchedEffect(LocalLifecycleOwner.current) {
        viewModel.onEvent(ActiveGameEvent.OnStart) // Avvia il timer e la logica iniziale
    }

    // Gestione della navigazione al completamento del gioco
    LaunchedEffect(uiState) {
        if (uiState == ActiveGameScreenState.COMPLETE) {
            onGameSolved() // Richiama il callback per tornare indietro o mostrare una schermata di vittoria
        }
    }

    // Gestione degli errori di caricamento/logica
    LaunchedEffect(error) {
        error?.let {
            onGameLoadError()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (showLoading) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Difficulty: ${difficulty.name}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Time: ${formatTime(timerState)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Griglia del Sudoku
                SudokuGrid(sudokuGrid) { x, y ->
                    viewModel.onEvent(ActiveGameEvent.onTileFocused(x, y))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Controlli di input
                InputControls(selectedTile) { input ->
                    viewModel.onEvent(ActiveGameEvent.onInput(input))
                }
            }
        }

        // Overlay della schermata di completamento se il gioco è risolto
        if (uiState == ActiveGameScreenState.COMPLETE && !showLoading) {
            GameCompletionScreen(
                timerState = timerState,
                difficulty = difficulty,
                isNewRecord = isNewRecord,
                onNewGameClick = {
                    viewModel.onEvent(ActiveGameEvent.OnNewGameClicked)
                    // Non richiamare onGameSolved qui, perché il ViewModel gestisce il passaggio a una nuova partita
                    // e la UI si aggiornerà di conseguenza allo stato di Loading/Active
                }
            )
        }
    }
}

@Composable
fun SudokuGrid(
    grid: List<SudokuTile>,
    onTileClick: (x: Int, y: Int) -> Unit
) {
    Column(
        modifier = Modifier
            .aspectRatio(1f) // Rende la griglia quadrata
            .padding(8.dp)
            .border(2.dp, Color.Black) // Bordo esterno della griglia
    ) {
        (0..8).forEach { y -> // Righe
            Row(modifier = Modifier.weight(1f)) {
                (0..8).forEach { x -> // Colonne
                    val tile = grid.firstOrNull { it.x == x && it.y == y }
                    SudokuTileView(
                        tile = tile,
                        onClick = onTileClick,
                        isBoldBorder = (x % 3 == 2 && x != 8) || (y % 3 == 2 && y != 8)
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.SudokuTileView(
    tile: SudokuTile?,
    onClick: (x: Int, y: Int) -> Unit,
    isBoldBorder: Boolean
) {
    val borderColor = if (isBoldBorder) Color.Black else Color.Gray
    val borderWidth = if (isBoldBorder) 2.dp else 1.dp

    val backgroundColor = when {
        tile?.hasFocus == true -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) // Colore per il focus
        else -> MaterialTheme.colorScheme.surface // Colore di default
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .border(borderWidth, borderColor)
            .background(backgroundColor)
            .clickable {
                tile?.let { onClick(it.x, it.y) }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (tile?.value == 0) "" else tile?.value.toString(),
            color = if (tile?.readOnly == true) Color.Black else MaterialTheme.colorScheme.primary, // Colore per numeri di partenza vs inseriti dall'utente
            fontSize = 24.sp,
            fontWeight = if (tile?.readOnly == true) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun InputControls(
    selectedTile: SudokuTile?,
    onInput: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Numeri da 1 a 9
        val numbers = (1..9).chunked(3)
        numbers.forEach { rowNumbers ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                rowNumbers.forEach { number ->
                    InputButton(number = number, onClick = onInput, enabled = selectedTile != null && !selectedTile.readOnly)
                }
            }
        }
        // Pulsante "Clear"
        Spacer(modifier = Modifier.height(8.dp))
        InputButton(text = "Clear", onClick = { onInput(0) }, enabled = selectedTile != null && !selectedTile.readOnly)
    }
}


@Composable
fun InputButton(
    number: Int? = null,
    text: String? = null,
    onClick: (Int) -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = { number?.let { onClick(it) } ?: onClick(0) }, // 0 per "Clear"
        enabled = enabled,
        modifier = Modifier
            .width(48.dp)
            .height(48.dp)
    ) {
        Text(text ?: number.toString(), fontSize = 18.sp)
    }
}


@Composable
fun GameCompletionScreen(
    timerState: Long,
    difficulty: DifficultyLevel,
    isNewRecord: Boolean,
    onNewGameClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(text = "Puzzle Solved!", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(text = "Difficulty: ${difficulty.name}", style = MaterialTheme.typography.titleLarge)
        Text(text = "Time: ${formatTime(timerState)}", style = MaterialTheme.typography.titleLarge)
        if (isNewRecord) {
            Text(text = "NEW RECORD!", style = MaterialTheme.typography.headlineSmall, color = Color.Green)
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onNewGameClick) {
            Text("Start New Game")
        }
    }
}

// Funzione helper per formattare il tempo (da secondi a HH:MM:SS)
fun formatTime(seconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(seconds)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(hours)
    val secs = seconds - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours)
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
