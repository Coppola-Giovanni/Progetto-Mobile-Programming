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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    activeGameViewModel: ActiveGameViewModel,
    navController: NavController, // NavController viene passato dalla MainActivity
    modifier: Modifier = Modifier,
) {

    val activeGameScreenState by activeGameViewModel.activeGameScreenState.collectAsState() // Usa activeGameViewModel
    val sudokuTiles by activeGameViewModel.sudokuTiles.collectAsState() // Usa activeGameViewModel
    val timerState by activeGameViewModel.timerState.collectAsState() // Usa activeGameViewModel
    val selectedTile by activeGameViewModel.selectedTile.collectAsState() // Usa activeGameViewModel
    val isSolved by activeGameViewModel.isSolved.collectAsState() // Usa activeGameViewModel
    val currentDifficulty by activeGameViewModel.currentDifficulty.collectAsState() // Usa activeGameViewModel
    val isNewRecord by activeGameViewModel.isNewRecord.collectAsState() // Usa activeGameViewModel

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START -> activeGameViewModel.onEvent(ActiveGameEvent.OnStart)
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> activeGameViewModel.onEvent(ActiveGameEvent.OnStop)
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = { // <<< AGGIUNGI IL BLOCCO ACTIONS QUI
                    IconButton(onClick = {
                        navController.navigate("user_preferences_screen") // Naviga alla nuova destinazione
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Settings, // Icona per le impostazioni
                            contentDescription = "Impostazioni utente",
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

                    Text(
                        text = "Difficoltà: ${currentDifficulty?.name ?: "N/A"}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp) // Add some padding if needed
                    )

                    SudokuGrid(
                        tiles = sudokuTiles,
                        selectedTile = selectedTile,
                        onTileClick = { x, y -> activeGameViewModel.onEvent(ActiveGameEvent.onTileFocused(x, y))  }
                    )

                    Spacer(Modifier.height(16.dp))

                    NumberInput(onNumberClick = { number ->
                        activeGameViewModel.onEvent(ActiveGameEvent.onInput(number))
                    })
                }
                ActiveGameScreenState.COMPLETE -> {
                    GameCompletionScreen(
                        timerState = timerState,
                        difficulty = currentDifficulty,
                        isNewRecord = isNewRecord,
                        onNewGameClick = { activeGameViewModel.onEvent(ActiveGameEvent.OnNewGameClicked) }
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

@Composable
fun SudokuGrid(
    tiles: List<SudokuTile>,
    selectedTile: SudokuTile?,
    onTileClick: (x: Int, y: Int) -> Unit
) {
    val gridSize = 9
    val thinLine = 1.dp
    val thickLine = 3.dp // Border for 3x3 blocks

    val borderColor = MaterialTheme.colorScheme.inversePrimary

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(8.dp)
            .border(thickLine, borderColor) // Overall outer border of the Sudoku grid
    ) {
        for (row in 0 until gridSize) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0 until gridSize) {
                    val tile = tiles.firstOrNull { it.x == col && it.y == row } ?: SudokuTile(col, row, 0, false, false)
                    val isSelected = selectedTile?.x == col && selectedTile?.y == row
                    val isInitial = tile.readOnly

                    // Determine border widths for this cell
                    val currentThinLine = 1.dp
                    val currentThickLine = 3.dp

                    // Left border is thick if it's the start of a 3x3 block (col 0, 3, 6)
                    val leftBorder = if (col % 3 == 0) currentThickLine else currentThinLine
                    // Top border is thick if it's the start of a 3x3 block (row 0, 3, 6)
                    val topBorder = if (row % 3 == 0) currentThickLine else currentThinLine
                    // Right border is thick if it's the end of a 3x3 block (col 2, 5) and not the very last column
                    val rightBorder = if ((col + 1) % 3 == 0 && col != gridSize - 1) currentThickLine else currentThinLine
                    // Bottom border is thick if it's the end of a 3x3 block (row 2, 5) and not the very last row
                    val bottomBorder = if ((row + 1) % 3 == 0 && row != gridSize - 1) currentThickLine else currentThinLine


                    SudokuCell(
                        tile = tile,
                        isSelected = isSelected,
                        isInitial = isInitial,
                        onClick = { onTileClick(col, row) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .drawBehind { // Use drawBehind for precise border drawing
                                // Draw top border
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = topBorder.toPx()
                                )
                                // Draw left border
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = leftBorder.toPx()
                                )
                                // Draw right border (only if not the very last column, as outer border handles it)
                                if (col != gridSize - 1) {
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(size.width, 0f),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = rightBorder.toPx()
                                    )
                                }
                                // Draw bottom border (only if not the very last row, as outer border handles it)
                                if (row != gridSize - 1) {
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        strokeWidth = bottomBorder.toPx()
                                    )
                                }
                            }
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
        isInitial -> MaterialTheme.colorScheme.primary
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row for numbers 1-5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 1..5) {
                // Passa il numero come String
                InputButton(displayValue = i.toString(), onClick = { onNumberClick(i) })
            }
        }
        Spacer(Modifier.height(8.dp)) // Add some vertical space between rows

        // Row for numbers 6-9 and Clear (X)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 6..9) {
                // Passa il numero come String
                InputButton(displayValue = i.toString(), onClick = { onNumberClick(i) })
            }
            // Per il pulsante "X", passa la stringa "X"
            InputButton(displayValue = "X", onClick = { onNumberClick(0) }) // 0 for clear
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
            .size(48.dp), // Dimensione compatta ma sufficiente
        contentPadding = PaddingValues(0.dp), // Elimina padding interno
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary, // Sfondo visibile
            contentColor = Color.Black // Testo nero, ben leggibile
        )
    ) {
        Text(
            text = displayValue,
            fontSize = 18.sp,
            color = Color.White // Assicura colore visibile
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
