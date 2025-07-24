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
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.sudokuMaster.ui.home.WinScreen
import com.sudokuMaster.ui.theme.isDark
import com.sudokuMaster.ui.theme.DarkModeSelectedCellHighlight
import com.sudokuMaster.ui.userpreferences.SoundPlayer
import androidx.compose.ui.platform.LocalContext
import com.sudokuMaster.ui.userpreferences.UserPreferencesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveGameScreen(
    activeGameViewModel: ActiveGameViewModel,
    userPreferencesViewModel: UserPreferencesViewModel,
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
    val isNotesMode by activeGameViewModel.isNotesMode.collectAsState()
    val userPreferences by userPreferencesViewModel.userPreferencesFlow.collectAsState(initial = null)
    val soundPlayer = SoundPlayer(LocalContext.current)

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
    val isCurrentThemeDark = MaterialTheme.colorScheme.isDark()

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Questo padding è fondamentale per posizionare il contenuto sotto la TopAppBar
        ) {
            // Sfondo immagine
            Image(
                painter = painterResource(
                    id = if (isCurrentThemeDark) R.drawable.dark_mode_background else R.drawable.light_mode_background
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Il tuo 'when (activeGameScreenState)' va qui dentro
            when (activeGameScreenState) {
                ActiveGameScreenState.LOADING -> {
                    Column(
                        modifier = Modifier // Rimosso 'modifier' dal parametro della Column
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                        // Aggiusta il colore del testo per la leggibilità sullo sfondo
                        Text(text = stringResource(R.string.loading_sudoku), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                ActiveGameScreenState.ACTIVE -> {
                    if (isLandscape) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
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
                                    selectedX = selectedTile?.x,
                                    selectedY = selectedTile?.y,
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
                                        color = MaterialTheme.colorScheme.onBackground, // Aggiusta il colore per lo sfondo
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.difficolt,
                                            currentDifficulty?.name ?: "N/A"
                                        ),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onBackground, // Aggiusta il colore per lo sfondo
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

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            if (userPreferences?.soundEnabled == true) {
                                                soundPlayer.playSound(R.raw.button_click_sound)
                                            }
                                            activeGameViewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                    ) {
                                        Text(stringResource(R.string.suggest_move))
                                    }
                                    Button(
                                        onClick = {
                                            if (userPreferences?.soundEnabled == true) {
                                                soundPlayer.playSound(R.raw.button_click_sound)
                                            }
                                            activeGameViewModel.onEvent(ActiveGameEvent.OnToggleNotesMode) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isNotesMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                            contentColor = if (isNotesMode) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                    ) {
                                        Text(if (isNotesMode) stringResource(R.string.notes_mode_on) else stringResource(R.string.notes_mode_off))
                                    }
                                }

                                NumberInput(onNumberClick = { number ->
                                    if (userPreferences?.soundEnabled == true) {
                                        soundPlayer.playSound(R.raw.button_click_sound)
                                    }
                                    if (isNotesMode) {
                                        activeGameViewModel.onEvent(ActiveGameEvent.onNoteInput(number))
                                    } else {
                                        activeGameViewModel.onEvent(ActiveGameEvent.onInput(number))
                                    }
                                })
                            }
                        }
                    } else { // Portrait
                        Column(
                            modifier = Modifier
                                .fillMaxSize(),
                            //.padding(paddingValues), // Rimosso, il padding è già sul Box esterno
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            Spacer(modifier = Modifier.weight(0.5f))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.time, formatTime(timerState)),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground, // Aggiusta il colore per lo sfondo
                                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.difficolt2,
                                        currentDifficulty?.name ?: "N/A"
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground, // Aggiusta il colore per lo sfondo
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
                                selectedX = selectedTile?.x,
                                selectedY = selectedTile?.y,
                                onTileClick = { x, y -> activeGameViewModel.onEvent(ActiveGameEvent.onTileFocused(x, y)) }
                            )

                            Spacer(Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (userPreferences?.soundEnabled == true) {
                                            soundPlayer.playSound(R.raw.button_click_sound)
                                        }
                                        activeGameViewModel.onEvent(ActiveGameEvent.OnSuggestMoveClicked) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                ) {
                                    Text(stringResource(R.string.suggest_move))
                                }
                                Button(
                                    onClick = {
                                        if (userPreferences?.soundEnabled == true) {
                                            soundPlayer.playSound(R.raw.button_click_sound)
                                        }
                                        activeGameViewModel.onEvent(ActiveGameEvent.OnToggleNotesMode) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isNotesMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                        contentColor = if (isNotesMode) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                                ) {
                                    Text(if (isNotesMode) stringResource(R.string.notes_mode_on) else stringResource(R.string.notes_mode_off))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            NumberInput(onNumberClick = { number ->
                                if (userPreferences?.soundEnabled == true) {
                                    soundPlayer.playSound(R.raw.button_click_sound)
                                }
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
                    WinScreen(
                        navController = navController,
                        activeGameViewModel = activeGameViewModel
                    )
                    LaunchedEffect(Unit) {
                        if (userPreferences?.soundEnabled == true) {
                            soundPlayer.playSound(R.raw.win_sound)
                        }
                    }
                }
                ActiveGameScreenState.ERROR -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        //.padding(paddingValues), // Rimosso, il padding è già sul Box esterno
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
    selectedX: Int?,
    selectedY: Int?,
    onTileClick: (x: Int, y: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridSize = 9
    val thinLine = 1.dp
    val thickLine = 3.dp
    val borderColor = MaterialTheme.colorScheme.secondary
    val isCurrentThemeDark = MaterialTheme.colorScheme.isDark()

    val highlightedColor = if (isCurrentThemeDark) {
        // Colore per l'evidenziazione in modalità scura
        DarkModeSelectedCellHighlight.copy(alpha = 0.5f)
    } else {
        // Colore per l'evidenziazione in modalità chiara
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    }

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
                    val isSelected = selectedX == col && selectedY == row
                    val isInitial = tile.readOnly

                    // Nuova logica per l'evidenziazione
                    val isHighlighted = selectedX != null && selectedY != null &&
                            (row == selectedY || col == selectedX ||
                                    (row / 3 == selectedY / 3 && col / 3 == selectedX / 3))

                    val topBorder = if (row % 3 == 0) thickLine else thinLine
                    val leftBorder = if (col % 3 == 0) thickLine else thinLine
                    val rightBorder = if ((col + 1) % 3 == 0) thickLine else thinLine
                    val bottomBorder = if ((row + 1) % 3 == 0) thickLine else thinLine

                    SudokuCell(
                        tile = tile,
                        isSelected = isSelected,
                        isInitial = isInitial,
                        isHighlighted = isHighlighted,
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
    isHighlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentThemeDark = MaterialTheme.colorScheme.isDark()

    val backgroundColor = when {
        isSelected -> if (isCurrentThemeDark) DarkModeSelectedCellHighlight.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        isHighlighted -> if (isCurrentThemeDark) DarkModeSelectedCellHighlight.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        tile.isInvalid -> Color.Red
        isInitial -> if (isCurrentThemeDark) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable(onClick = onClick),
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
        } else {
            if (tile.notes.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                                            fontSize = 10.sp
                                        ),
                                        color = textColor,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center
                                    )
                                } else {
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
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = displayValue,
            fontSize = 18.sp,
        )
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(seconds)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
    val secs = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}