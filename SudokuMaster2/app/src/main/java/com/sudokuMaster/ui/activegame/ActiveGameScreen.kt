package com.sudokuMaster.ui.activegame

import android.annotation.SuppressLint
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
import androidx.lifecycle.viewmodel.compose.viewModel // Importa questa funzione
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.SudokuMaster.R
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.domain.getHash
import java.util.concurrent.TimeUnit


@Composable
fun ActiveGameScreen(
    viewModelFactory: ActiveGameViewModelFactory,
    modifier: Modifier = Modifier,
) {
    val viewModel: ActiveGameViewModel = viewModel(
        factory = viewModelFactory)

    // Osserva gli StateFlow del ViewModel
    val screenState by viewModel.screenState.collectAsState()
    val boardState by viewModel.boardState.collectAsState()
    val timerState by viewModel.timerState.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()
    val isNewRecord by viewModel.isNewRecord.collectAsState()


    // Gestione degli eventi di navigazione o errori (se necessario, in futuro)
    // val lifecycleOwner = LocalLifecycleOwner.current
    // DisposableEffect(lifecycleOwner) {
    //     val observer = LifecycleEventObserver { _, event ->
    //         if (event == Lifecycle.Event.ON_STOP) {
    //             viewModel.onEvent(ActiveGameEvent.OnStop)
    //         }
    //     }
    //     lifecycleOwner.lifecycle.addObserver(observer)
    //     onDispose {
    //         lifecycleOwner.lifecycle.removeObserver(observer)
    //     }
    // }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        when (screenState) {
            ActiveGameScreenState.LOADING -> {
                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                Text(text = stringResource(R.string.loading_sudoku), style = MaterialTheme.typography.titleMedium)
            }
            ActiveGameScreenState.ACTIVE -> {
                GameHeader(difficulty = difficulty, timerState = timerState)
                Spacer(Modifier.height(16.dp))
                SudokuBoard(
                    boardState = boardState,
                    onTileFocused = { x, y -> viewModel.onEvent(ActiveGameEvent.OnTileFocused(x, y)) }
                )
                Spacer(Modifier.height(16.dp))
                NumberPad(onInput = { input -> viewModel.onEvent(ActiveGameEvent.OnInput(input)) })
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.onEvent(ActiveGameEvent.OnStop) }) {
                    Text(stringResource(R.string.save_exit))
                }
            }
            ActiveGameScreenState.COMPLETE -> {
                GameCompletionScreen(
                    timerState = timerState,
                    difficulty = difficulty,
                    isNewRecord = isNewRecord,
                    onNewGameClick = { viewModel.onEvent(ActiveGameEvent.OnNewGameClicked) }
                )
            }
            ActiveGameScreenState.ERROR -> {
                //
            }
        }
    }
}

@Composable
fun getLocalizedDifficulty(difficulty: DifficultyLevel): String {
    return when (difficulty) {
        DifficultyLevel.EASY -> {
            stringResource(R.string.difficulty_easy)
        }
        DifficultyLevel.MEDIUM -> {
            stringResource(R.string.difficulty_medium)
        }
        DifficultyLevel.HARD -> {
            stringResource(R.string.difficulty_hard)
        }
        else -> stringResource(R.string.difficulty_hard)
    }
}




@Composable
fun GameHeader(difficulty: DifficultyLevel, timerState: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(R.string.Difficulty, getLocalizedDifficulty(difficulty)), style = MaterialTheme.typography.titleMedium)
        Text(text = formatTime(timerState), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun SudokuBoard(
    boardState: HashMap<Int, SudokuTile>,
    onTileFocused: (x: Int, y: Int) -> Unit
) {
    val blockSize = 3 // Per Sudoku 9x9, il blocco è 3x3
    val boardSize = 9

    Column(modifier = Modifier
        .fillMaxWidth(0.9f)
        .aspectRatio(1f)
        .border(2.dp, Color.Black)
    ) {
        (0 until boardSize).forEach { y ->
            Row(modifier = Modifier.weight(1f)) {
                (0 until boardSize).forEach { x ->
                    val tile = boardState[getHash(x, y)] ?: SudokuTile(x, y, 0,
                        hasFocus = false,
                        readOnly = false
                    )
                    val isBlockBorder = (x % blockSize == 0 && x != 0) || (y % blockSize == 0 && y != 0)
                    val borderModifier = if (isBlockBorder) Modifier.border(1.dp, Color.Black) else Modifier.border(0.5.dp, Color.Gray)

                    SudokuCell(
                        tile = tile,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(borderModifier),
                        onClick = onTileFocused
                    )
                }
            }
        }
    }
}

@Composable
fun SudokuCell(
    tile: SudokuTile,
    modifier: Modifier = Modifier,
    onClick: (x: Int, y: Int) -> Unit
) {
    val backgroundColor = when {
        tile.hasFocus -> Color.LightGray
        tile.readOnly -> Color.LightGray.copy(alpha = 0.5f)
        else -> Color.White
    }

    val textColor = if (tile.readOnly) Color.DarkGray else Color.Black

    Box(
        modifier = modifier
            .background(backgroundColor)
            .clickable { onClick(tile.x, tile.y) }
            .aspectRatio(1f), // Assicura che la cella sia quadrata
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (tile.value != 0) tile.value.toString() else "",
            fontSize = 20.sp,
            fontWeight = if (tile.readOnly) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NumberPad(onInput: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Riga 1: 1 2 3
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            (1..3).forEach { number ->
                NumberButton(number = number, onInput = onInput)
            }
        }
        Spacer(Modifier.height(8.dp))
        // Riga 2: 4 5 6
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            (4..6).forEach { number ->
                NumberButton(number = number, onInput = onInput)
            }
        }
        Spacer(Modifier.height(8.dp))
        // Riga 3: 7 8 9
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            (7..9).forEach { number ->
                NumberButton(number = number, onInput = onInput)
            }
        }
        Spacer(Modifier.height(8.dp))
        // Riga 4: Clear (0)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            NumberButton(number = 0, onInput = onInput, text = stringResource(R.string.clear))
        }
    }
}

@Composable
fun NumberButton(number: Int, onInput: (Int) -> Unit, text: String? = null) {
    Button(
        onClick = { onInput(number) },
        modifier = Modifier
            .width(64.dp)
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
        Text(text = stringResource(R.string.Difficulty, getLocalizedDifficulty(difficulty)), style = MaterialTheme.typography.titleLarge)
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

// Funzione helper per formattare il tempo (da secondi a HH:MM:SS)
@SuppressLint("DefaultLocale")
fun formatTime(seconds: Long): String {
    val hours = TimeUnit.SECONDS.toHours(seconds)
    val minutes = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.HOURS.toMinutes(hours)
    val secs = seconds - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours)
    return String.format("%02d:%02d:%02d", hours, minutes, secs)
}
