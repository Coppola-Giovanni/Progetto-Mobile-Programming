package com.sudokuMaster.ui.activegame

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.sudokuMaster.data.DifficultyLevel
import java.util.concurrent.TimeUnit
import androidx.navigation.NavController
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    viewModelFactory: ActiveGameViewModelFactory,
    navController: NavController, // NavController viene passato dalla MainActivity
    modifier: Modifier = Modifier,
) {
    val viewModel: ActiveGameViewModel = viewModel(
        factory = viewModelFactory
    )

    val activeGameScreenState by viewModel.activeGameScreenState.collectAsState()
    val sudokuTiles by viewModel.sudokuTiles.collectAsState() // Ora risolto dal ViewModel
    val timerState by viewModel.timerState.collectAsState()
    val selectedTile by viewModel.selectedTile.collectAsState()
    val isSolved by viewModel.isSolved.collectAsState()
    val currentDifficulty by viewModel.currentDifficulty.collectAsState() // Ora risolto dal ViewModel
    val isNewRecord by viewModel.isNewRecord.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START -> viewModel.onEvent(ActiveGameEvent.OnStart)
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> viewModel.onEvent(ActiveGameEvent.OnStop)
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Potresti mettere un titolo qui, ad esempio "Sudoku"
                    Text("Sudoku Master", color = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Quando si clicca la freccia indietro, si torna alla schermata precedente
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (activeGameScreenState) {
                ActiveGameScreenState.LOADING -> {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Text(text = "Caricamento Sudoku...", style = MaterialTheme.typography.titleMedium)
                }
                ActiveGameScreenState.ACTIVE -> {
                    Text(
                        text = "Tempo: ${formatTime(timerState)}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )

                    SudokuGrid(
                        tiles = sudokuTiles,
                        selectedTile = selectedTile,
                        onTileClick = { x, y -> viewModel.onEvent(ActiveGameEvent.onTileFocused(x, y)) }
                    )

                    Spacer(Modifier.height(16.dp))

                    NumberInput(onNumberClick = { number ->
                        viewModel.onEvent(ActiveGameEvent.onInput(number))
                    })
                }
                ActiveGameScreenState.COMPLETE -> {
                    GameCompletionScreen(
                        timerState = timerState,
                        difficulty = currentDifficulty,
                        isNewRecord = isNewRecord,
                        onNewGameClick = { viewModel.onEvent(ActiveGameEvent.OnNewGameClicked) }
                    )
                }
                ActiveGameScreenState.ERROR -> {
                    // Puoi aggiungere una UI di errore più sofisticata qui
                    Text(
                        text = "Si è verificato un errore durante il caricamento del gioco.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { navController.popBackStack() }) { // Torna alla home
                        Text("Torna alla Home")
                    }
                }
            }
        }
    }
}

// ... (SudokuGrid, SudokuCell, NumberInput, InputButton, GameCompletionScreen, formatTime come forniti) ...

@Composable
fun SudokuGrid(
    tiles: List<SudokuTile>,
    selectedTile: SudokuTile?,
    onTileClick: (x: Int, y: Int) -> Unit
) {
    val gridSize = 9
    // val cellSize = 40.dp // Non strettamente necessario se usi weight e aspectRatio

    Column(
        modifier = Modifier
            .aspectRatio(1f) // Rendi la griglia quadrata
            .padding(8.dp)
            .border(2.dp, MaterialTheme.colorScheme.inversePrimary) // Bordo esterno
    ) {
        for (row in 0 until gridSize) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0 until gridSize) {
                    val tile = tiles.firstOrNull { it.x == col && it.y == row } ?: SudokuTile(col, row, 0, false, false)
                    val isSelected = selectedTile?.x == col && selectedTile?.y == row
                    val isInitial = tile.readOnly

                    SudokuCell(
                        tile = tile,
                        isSelected = isSelected,
                        isInitial = isInitial,
                        onClick = { onTileClick(col, row) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(
                                width = 1.dp, // Bordo sottile per le celle
                                color = MaterialTheme.colorScheme.inversePrimary
                            )
                        // Rimosso il secondo .then(Modifier.border(...)) duplicato
                    )
                }
            }
        }
    }
}

@Composable
fun SudokuCell(
    tile: SudokuTile,
    isSelected: Boolean,
    isInitial: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isInitial -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.secondary
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(enabled = !isInitial, onClick = onClick), // Aggiunto enabled = !isInitial
        contentAlignment = Alignment.Center
    ) {
        if (tile.value != 0) {
            Text(
                text = tile.value.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = textColor
            )
        }
    }
}


@Composable
fun NumberInput(
    onNumberClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        for (i in 1..9) {
            InputButton(number = i, onClick = { onNumberClick(i) })
        }
        InputButton(text = "X", onClick = { onNumberClick(0) }) // 0 per cancellare
    }
}

@Composable
fun InputButton(
    number: Int? = null,
    text: String? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
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

fun formatTime(seconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(seconds)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}