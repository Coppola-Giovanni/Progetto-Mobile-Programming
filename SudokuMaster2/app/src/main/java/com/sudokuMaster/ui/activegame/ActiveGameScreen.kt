package com.sudokuMaster.ui.activegame

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.res.stringResource
import com.SudokuMaster.R
import com.sudokuMaster.data.DifficultyLevel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    viewModelFactory: ActiveGameViewModelFactory,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActiveGameViewModel = viewModel(factory = viewModelFactory)

    val activeGameScreenState by viewModel.activeGameScreenState.collectAsState()
    val sudokuTiles by viewModel.sudokuTiles.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val selectedTile by viewModel.selectedTile.collectAsState()
    val isSolved by viewModel.isSolved.collectAsState()
    val currentDifficulty by viewModel.currentDifficulty.collectAsState()
    val isNewRecord by viewModel.isNewRecord.collectAsState()

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

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
                    Text(stringResource(R.string.app_name), color = MaterialTheme.colorScheme.onPrimaryContainer)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_to_home),
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
                .padding(paddingValues)
        ) {
            when (activeGameScreenState) {
                ActiveGameScreenState.LOADING -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                        Text(
                            text = stringResource(R.string.loading_sudoku),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                ActiveGameScreenState.ACTIVE -> {
                    if (isPortrait) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = stringResource(R.string.time, formatTime(timerState)),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )

                            SudokuGrid(
                                tiles = sudokuTiles,
                                selectedTile = selectedTile,
                                onTileClick = { x, y -> viewModel.onEvent(ActiveGameEvent.onTileFocused(x, y)) }
                            )

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.suggest_move))
                            }

                            Spacer(Modifier.height(16.dp))

                            NumberInput(onNumberClick = { number ->
                                viewModel.onEvent(ActiveGameEvent.onInput(number))
                            })
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SudokuGrid(
                                tiles = sudokuTiles,
                                selectedTile = selectedTile,
                                onTileClick = { x, y -> viewModel.onEvent(ActiveGameEvent.onTileFocused(x, y)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .heightIn(400.dp)

                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Top,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.time, formatTime(timerState)),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Button(
                                    onClick = { viewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.suggest_move))
                                }

                                Spacer(Modifier.height(16.dp))

                                NumberInput(onNumberClick = { number ->
                                    viewModel.onEvent(ActiveGameEvent.onInput(number))
                                })
                            }
                        }
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
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.si_verificato_un_errore_durante_il_caricamento_del_gioco),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { navController.popBackStack() }) {
                            Text(stringResource(R.string.back_to_home))
                        }
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
        .padding(end=8.dp)
        .widthIn(max=300.dp)
) {
    val gridSize = 9
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
            .border(2.dp, MaterialTheme.colorScheme.inversePrimary)
    ) {
        for (row in 0 until gridSize) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until gridSize) {
                    val tile = tiles.firstOrNull { it.x == col && it.y == row }
                        ?: SudokuTile(col, row, 0, false, false)
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
                            .border(1.dp, MaterialTheme.colorScheme.inversePrimary)
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
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.secondary
    }

    val textColor = if (isInitial) {
        MaterialTheme.colorScheme.onSurface
    } else {
        Color.Black
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
            InputButton(displayValue = "X", onClick = { onNumberClick(0) }) // 0 to clear
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
        modifier = Modifier.size(48.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = Color.Black
        )
    ) {
        Text(text = displayValue, fontSize = 18.sp, color = Color.Black)
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
        Text(
            text = stringResource(R.string.puzzle_solved),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))
        Text(text = "Difficulty: ${difficulty.name}", style = MaterialTheme.typography.titleLarge)
        Text(text = stringResource(R.string.time, formatTime(timerState)), style = MaterialTheme.typography.titleLarge)
        if (isNewRecord) {
            Text(text = stringResource(R.string.new_record), style = MaterialTheme.typography.headlineSmall, color = Color.Green)
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onNewGameClick) {
            Text(stringResource(R.string.start_new_game))
        }
    }
}

fun formatTime(seconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(seconds)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
