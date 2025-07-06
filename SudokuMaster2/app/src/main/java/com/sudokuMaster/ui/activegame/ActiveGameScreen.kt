package com.sudokuMaster.ui.activegame

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.SudokuMaster.R
import com.sudokuMaster.data.DifficultyLevel
import java.util.concurrent.TimeUnit
import androidx.compose.ui.graphics.luminance


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
                    // --- Layout per Landscape ---
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 8.dp, vertical = 4.dp), // Padding generale per Landscape
                        verticalAlignment = Alignment.CenterVertically // Centra verticalmente l'intera riga
                    ) {
                        // Sudoku Grid (Left side)
                        Column(
                            modifier = Modifier
                                .weight(0.6f) // Maggiore spazio per la griglia
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
                                    .padding(end = 8.dp) // Spazio tra griglia e controlli
                            )
                        }

                        // Controls (Right side)
                        Column(
                            modifier = Modifier
                                .weight(0.4f) // Meno spazio per i controlli
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceAround // Distribuisce lo spazio
                        ) {
                            // Timer and Difficulty
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.time, formatTime(timerState)),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.difficolt3,
                                        currentDifficulty?.name ?: "N/A"
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            // Suggestion Button
                            Button(
                                onClick = { activeGameViewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp) // Padding adeguato
                            ) {
                                Text(stringResource(R.string.suggest_move))
                            }

                            // Number Input
                            NumberInput(onNumberClick = { number ->
                                activeGameViewModel.onEvent(ActiveGameEvent.onInput(number))
                            })
                        }
                    }
                } else {
                    // --- Layout per Portrait ---
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround // Distribuisce lo spazio in verticale
                    ) {
                        // Usiamo un Spacer con weight per spingere gli elementi verso l'alto
                        Spacer(modifier = Modifier.weight(0.5f)) // Spazio superiore

                        // Timer and Difficulty (raggruppati)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.time, formatTime(timerState)),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp) // Spostato un po' più su
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

                        SudokuGrid(
                            tiles = sudokuTiles,
                            selectedTile = selectedTile,
                            onTileClick = { x, y -> activeGameViewModel.onEvent(ActiveGameEvent.onTileFocused(x, y)) }
                        )

                        Spacer(Modifier.height(16.dp)) // Spazio tra griglia e suggestion button

                        Button(
                            onClick = { activeGameViewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(stringResource(R.string.suggest_move))
                        }

                        // Usiamo un Spacer per dare respiro ai bottoni se necessario
                        Spacer(modifier = Modifier.height(16.dp))

                        NumberInput(onNumberClick = { number ->
                            activeGameViewModel.onEvent(ActiveGameEvent.onInput(number))
                        })

                        // Usiamo un Spacer con weight per assicurare che i bottoni non siano schiacciati in basso
                        Spacer(modifier = Modifier.weight(0.5f)) // Spazio inferiore
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
    val thickLine = 3.dp // Border for 3x3 blocks

    val borderColor = MaterialTheme.colorScheme.inversePrimary

    // Rimuoviamo il .border() esterno dalla Column, perché lo gestiamo cella per cella.
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp) // Lascia questo padding per distanziare la griglia dal resto
    ) {
        for (row in 0 until gridSize) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (col in 0 until gridSize) {
                    val tile = tiles.firstOrNull { it.x == col && it.y == row } ?: SudokuTile(col, row, 0, false, false)
                    val isSelected = selectedTile?.x == col && selectedTile?.y == row
                    val isInitial = tile.readOnly

                    // Determina lo spessore del bordo per ogni lato della cella
                    // Bordo superiore: spesso se riga 0, 3, 6 (inizio di un blocco 3x3 o griglia)
                    val topBorder = if (row % 3 == 0) thickLine else thinLine
                    // Bordo sinistro: spesso se colonna 0, 3, 6 (inizio di un blocco 3x3 o griglia)
                    val leftBorder = if (col % 3 == 0) thickLine else thinLine

                    // Bordo destro: spesso se colonna 2, 5, 8 (fine di un blocco 3x3 o griglia)
                    val rightBorder = if ((col + 1) % 3 == 0) thickLine else thinLine
                    // Bordo inferiore: spesso se riga 2, 5, 8 (fine di un blocco 3x3 o griglia)
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
                                // Draw right border
                                drawLine(
                                    color = borderColor,
                                    start = Offset(size.width, 0f),
                                    end = Offset(size.width, size.height),
                                    strokeWidth = rightBorder.toPx()
                                )
                                // Draw bottom border
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
                // Usa la costante definita in Colors.kt per coerenza
                MaterialTheme.colorScheme.onSurfaceVariant // Un colore che contrasta con la superficie, ma è per gli elementi selezionati.
                // Ho usato onSurfaceVariant come placeholder, ma potresti volerlo definire
                // come un nuovo colore nel tuo ColorScheme, o semplicemente usare SelectedTileBackgroundDark
                // Oppure, se vuoi usare la tua costante:
                // SelectedTileBackgroundDark // Assicurati di importare SelectedTileBackgroundDark se non è già accessibile
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            }
        }
        else -> MaterialTheme.colorScheme.secondary // Sfondo normale della cella
    }

    val textColor = when {
        isInitial -> MaterialTheme.colorScheme.onPrimary // <-- CORRETTO: Ora userà il ciano in Dark Mode
        else -> MaterialTheme.colorScheme.onSurface      // <-- CORRETTO: Ora userà il bianco in Dark Mode, nero in Light Mode
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
                color = textColor // Usa il colore del testo ottenuto dal tema
            )
        }
    }
}

fun ColorScheme.isDark(): Boolean {
    // Un modo semplice per determinare se il tema è scuro è controllare la luminanza del colore di sfondo.
    // Valori di luminanza bassi indicano colori scuri.
    // 0.5 è una soglia comune, ma puoi aggiustarla.
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
        // Row for numbers 1-5
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 1..5) {
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
                InputButton(displayValue = i.toString(), onClick = { onNumberClick(i) })
            }
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

fun formatTime(seconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(seconds)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
