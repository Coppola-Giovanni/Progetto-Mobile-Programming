package com.sudokuMaster.ui.activegame

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.SudokuMaster.R
import com.sudokuMaster.data.DifficultyLevel
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.ColorScheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    activeGameViewModel: ActiveGameViewModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {

    val activeGameScreenState by activeGameViewModel.activeGameScreenState.collectAsState()
    val sudokuTiles by activeGameViewModel.sudokuTiles.collectAsState()
    val timerState by activeGameViewModel.timerState.collectAsState()
    val selectedTile by activeGameViewModel.selectedTile.collectAsState()
    val isSolved by activeGameViewModel.isSolved.collectAsState()
    val currentDifficulty by activeGameViewModel.currentDifficulty.collectAsState()
    val isNewRecord by activeGameViewModel.isNewRecord.collectAsState()
    val hasInvalidTiles by activeGameViewModel.hasInvalidTiles.collectAsState()
    val isNotesMode by activeGameViewModel.isNotesMode.collectAsState() // <<< NUOVO: Osserva lo stato della modalità note

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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("user_preferences_screen")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.impostazioni_utente),
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
        when (activeGameScreenState) {
            ActiveGameScreenState.LOADING -> {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Text(text = stringResource(R.string.loading_sudoku), style = MaterialTheme.typography.titleMedium)
                }
            }
            ActiveGameScreenState.ACTIVE -> {
                if (isLandscape) {

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            SudokuGrid(
                                tiles = sudokuTiles,
                                selectedTile = selectedTile,
                                onTileClick = { x, y -> activeGameViewModel.onEvent(ActiveGameEvent.onTileFocused(x, y)) },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(end = 8.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.time, formatTime(timerState)),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.difficolt,
                                        currentDifficulty?.name ?: "N/A"
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            if (hasInvalidTiles) {
                                Text(
                                    text = stringResource(R.string.invalid_numbers_message),
                                    color = Color.Red,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            // --- CONTROLLI AGGIUNTI ---
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { activeGameViewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                ) {
                                    Text(stringResource(R.string.suggest_move))
                                }
                                Button(
                                    onClick = { activeGameViewModel.onEvent(ActiveGameEvent.OnToggleNotesMode) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isNotesMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                        contentColor = if (isNotesMode) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                ) {
                                    Text(if (isNotesMode) stringResource(R.string.notes_mode_on) else stringResource(R.string.notes_mode_off)) // R.string.notes_mode_on/off vanno aggiunte in strings.xml
                                }
                            }
                            // --- FINE CONTROLLI AGGIUNTI ---

                            NumberInput(
                                onNumberClick = { number ->
                                    // Invia l'evento corretto in base alla modalità corrente
                                    if (isNotesMode) {
                                        activeGameViewModel.onEvent(ActiveGameEvent.onNoteInput(number))
                                    } else {
                                        activeGameViewModel.onEvent(ActiveGameEvent.onInput(number))
                                    }
                                }
                            )
                        }
                    }
                } else { // Portrait
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Spacer(modifier = Modifier.weight(0.5f))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.time, formatTime(timerState)),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                            )
                            Text(
                                text = stringResource(
                                    R.string.difficolt2,
                                    currentDifficulty?.name ?: "N/A"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        if (hasInvalidTiles) {
                            Text(
                                text = stringResource(R.string.invalid_numbers_message),
                                color = Color.Red,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        SudokuGrid(
                            tiles = sudokuTiles,
                            selectedTile = selectedTile,
                            onTileClick = { x, y -> activeGameViewModel.onEvent(ActiveGameEvent.onTileFocused(x, y)) }
                        )

                        Spacer(Modifier.height(16.dp))

                        // --- CONTROLLI AGGIUNTI ---
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { activeGameViewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            ) {
                                Text(stringResource(R.string.suggest_move))
                            }
                            Button(
                                onClick = { activeGameViewModel.onEvent(ActiveGameEvent.OnToggleNotesMode) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isNotesMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    contentColor = if (isNotesMode) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                            ) {
                                Text(if (isNotesMode) stringResource(R.string.notes_mode_on) else stringResource(R.string.notes_mode_off))
                            }
                        }
                        // --- FINE CONTROLLI AGGIUNTI ---

                        Spacer(modifier = Modifier.height(16.dp))

                        NumberInput(onNumberClick = { number ->
                            if (isNotesMode) {
                                activeGameViewModel.onEvent(ActiveGameEvent.onNoteInput(number))
                            } else {
                                activeGameViewModel.onEvent(ActiveGameEvent.onInput(number))
                            }
                        })

                        Spacer(modifier = Modifier.weight(0.5f))
                    }
                }
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
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues),
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


@Composable
fun SudokuGrid(
    tiles: List<SudokuTile>,
    selectedTile: SudokuTile?,
    onTileClick: (x: Int, y: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridSize = 9
    val thinLine = 1.dp
    val thickLine = 3.dp
    val borderColor = MaterialTheme.colorScheme.inversePrimary

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        for (row in 0 until gridSize) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0 until gridSize) {
                    val tile = tiles.firstOrNull { it.x == col && it.y == row } ?: SudokuTile(col, row, 0, false, false)
                    val isSelected = selectedTile?.x == col && selectedTile?.y == row
                    val isInitial = tile.readOnly

                    val topBorder = if (row % 3 == 0) thickLine else thinLine
                    val leftBorder = if (col % 3 == 0) thickLine else thinLine
                    val rightBorder = if ((col + 1) % 3 == 0) thickLine else thinLine
                    val bottomBorder = if ((row + 1) % 3 == 0) thickLine else thinLine


                    SudokuCell(
                        tile = tile,
                        isSelected = isSelected,
                        isInitial = isInitial,
                        onClick = { onTileClick(col, row) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .drawBehind {
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = topBorder.toPx()
                                )
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = leftBorder.toPx()
                                )
                                drawLine(
                                    color = borderColor,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = rightBorder.toPx()
                                )
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, size.height),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = bottomBorder.toPx()
                                )
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
    val isCurrentThemeDark = MaterialTheme.colorScheme.isDark()

    val backgroundColor = when {
        isSelected -> {
            if (isCurrentThemeDark) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            }
        }
        else -> MaterialTheme.colorScheme.secondary
    }

    val textColor = when {
        tile.isInvalid -> Color.Red
        isInitial -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(enabled = !isInitial, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (tile.value != 0) { // Se c'è un valore definitivo, mostralo
            Text(
                text = tile.value.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                color = textColor
            )
        } else { // Se la cella è vuota, mostra le note
            if (tile.notes.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Crea una griglia 3x3 per le note
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 3) {
                                val number = row * 3 + col + 1
                                if (tile.notes.contains(number)) {
                                    Text(
                                        text = number.toString(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 10.sp // Dimensione ridotta per le note
                                        ),
                                        color = textColor,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
                                    // Spazio vuoto se la nota non è presente
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun ColorScheme.isDark(): Boolean {
    return background.luminance() < 0.5f
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
            .size(48.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = displayValue,
            fontSize = 18.sp,
            color = Color.White
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
        Text(text = stringResource(R.string.puzzle_solved), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(text = stringResource(R.string.difficulty2, difficulty.name), style = MaterialTheme.typography.titleLarge)
        Text(text = stringResource(R.string.time,formatTime(timerState)), style = MaterialTheme.typography.titleLarge)
        if (isNewRecord) {
            Text(text = stringResource(R.string.new_record), style = MaterialTheme.typography.headlineSmall, color = Color.Green)
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onNewGameClick) {
            Text(stringResource(R.string.start_new_game))
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(seconds)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}