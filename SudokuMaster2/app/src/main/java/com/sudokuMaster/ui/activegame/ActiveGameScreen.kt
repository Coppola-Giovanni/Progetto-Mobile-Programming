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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.res.stringResource
import com.SudokuMaster.R
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    viewModelFactory: ActiveGameViewModelFactory,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActiveGameViewModel = viewModel(
        factory = viewModelFactory
    )

    val activeGameScreenState by viewModel.activeGameScreenState.collectAsState()
    val sudokuTiles by viewModel.sudokuTiles.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val selectedTile by viewModel.selectedTile.collectAsState()
    val isSolved by viewModel.isSolved.collectAsState()
    val currentDifficulty by viewModel.currentDifficulty.collectAsState()
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
                    Text("Sudoku Master", color = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                navigationIcon = {
                    IconButton(onClick = {
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

        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

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
                    if (isLandscape) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp), // Ridotto il padding generale della riga
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sudoku Grid a sinistra (reso più piccolo)
                            SudokuGrid(
                                tiles = sudokuTiles,
                                selectedTile = selectedTile,
                                onTileClick = { x, y -> viewModel.onEvent(ActiveGameEvent.onTileFocused(x, y)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(0.9f) // La griglia occupa solo il 90% dell'altezza disponibile
                                    .aspectRatio(1f) // Mantiene la proporzione quadrata
                                    .wrapContentSize(align = Alignment.Center) // Centra il contenuto se più piccolo
                                    .padding(2.dp) // Ridotto il padding interno alla griglia
                            )

                            // Controlli e informazioni a destra
                            Column(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .fillMaxHeight()
                                    .padding(4.dp), // Ridotto il padding per la colonna dei controlli
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceAround
                            ) {
                                Text(
                                    text = "Tempo: ${formatTime(timerState)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 4.dp) // Ridotto il padding del tempo
                                )

                                NumberInput(onNumberClick = { number ->
                                    viewModel.onEvent(ActiveGameEvent.onInput(number))
                                })
                                Button(
                                    onClick = { viewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp, vertical = 1.dp) // Ridotto padding del bottone
                                        .heightIn(min = 36.dp) // Altezza minima leggermente ridotta per il bottone
                                ) {
                                    Text(stringResource(R.string.suggest_move), fontSize = 12.sp) // Testo ancora più piccolo
                                }
                            }
                        }
                    } else {
                        // Layout per orientamento verticale (originale)
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
                        Button(
                            onClick = { viewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.suggest_move))
                        }

                        Spacer(Modifier.height(16.dp))
                    }
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
                    Text(
                        text = "Si è verificato un errore durante il caricamento del gioco.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Torna alla Home")
                    }
                }
            }
        }
    }
}

@Composable
fun SudokuGrid(
    tiles: List<SudokuTile>,
    selectedTile: SudokuTile?,
    onTileClick: (x: Int, y: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridSize = 9

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
            .border(2.dp, MaterialTheme.colorScheme.inversePrimary)
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
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.inversePrimary
                            )
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
        else -> MaterialTheme.colorScheme.secondary
    }

    val textColor = when {
        isInitial -> MaterialTheme.colorScheme.onSurface
        else ->Color.Black
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(enabled = !isInitial, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (tile.value != 0) {
            Text(
                text = tile.value.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp, // Ridotto ulteriormente a 18.sp
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 1..5) {
                InputButton(displayValue = i.toString(), onClick = { onNumberClick(i) })
            }
        }
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 6..9) {
                InputButton(displayValue = i.toString(), onClick = { onNumberClick(i) })
            }
            InputButton(displayValue = "X", onClick = { onNumberClick(0) })
        }
    }
}

@Composable
fun InputButton(
    displayValue: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp), // Ridotto leggermente la dimensione dei pulsanti
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = displayValue,
            fontSize = 16.sp, // Ridotto la dimensione del testo nei pulsanti
            color = Color.Black
        )
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