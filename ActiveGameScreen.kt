package com.sudokuMaster.ui.activegame

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.sudokuMaster.ui.components.AppToolbar
import com.SudokuMaster.R
import com.sudokuMaster.common.toTime
import com.sudokuMaster.ui.components.LoadingScreen
import com.sudokuMaster.ui.theme.activeGameSubtitle
import com.sudokuMaster.ui.theme.inputButton
import com.sudokuMaster.ui.theme.mutableSudokuSquare
import com.sudokuMaster.ui.theme.newGameSubtitle
import com.sudokuMaster.ui.theme.readOnlySudokuSquare
import com.sudokuMaster.ui.theme.textColorDark
import com.sudokuMaster.ui.theme.textColorLight
import com.sudokuMaster.ui.theme.userInputtedNumberDark
import com.sudokuMaster.ui.theme.userInputtedNumberLight

enum class ActiveGameScreenState {
    LOADING,
    ACTIVE,
    COMPLETE
}

@Composable
fun ActiveGameScreen(
    onEventHandler: (ActiveGameEvent) -> Unit,
    viewModel: ActiveGameViewModel
) {
    val contentTransitionState = remember {
        MutableTransitionState(                    //for animations
            ActiveGameScreenState.LOADING
        )
    }

    viewModel.subContentState = {
        contentTransitionState.targetState = it
    }

    val transition = updateTransition(contentTransitionState)

    val loadingAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300)}
    ) {
        if (it == ActiveGameScreenState.LOADING) 1f else 0f
    }

    val activeAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300)}
    ) {
        if (it == ActiveGameScreenState.ACTIVE) 1f else 0f
    }

    val completeAlpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 300)}
    ) {
        if (it == ActiveGameScreenState.COMPLETE) 1f else 0f
    }

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.primary)
            .fillMaxHeight()
    ) {
        AppToolbar(modifier = Modifier.wrapContentHeight(),
            title = stringResource(R.string.app_name)
        ) {
            NewGameIcon(onEventHandler = onEventHandler)
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 4.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            when (contentTransitionState.currentState) {
                ActiveGameScreenState.ACTIVE -> Box(
                    Modifier.alpha(activeAlpha)
                ) {
                    GameContent(
                        onEventHandler,
                        viewModel
                    )
                }

                ActiveGameScreenState.LOADING -> Box(
                    Modifier.alpha(loadingAlpha)
                ) {
                    LoadingScreen()
                }

                ActiveGameScreenState.COMPLETE -> Box(
                    Modifier.alpha(activeAlpha)
                ) {
                    GameCompleteContent(
                        viewModel.timerState,
                        viewModel.isNewRecordState
                    )
                }
            }
        }
    }
}

@Composable
fun NewGameIcon(onEventHandler: (ActiveGameEvent) -> Unit) {
    Icon(
        imageVector = Icons.Filled.Add,
        tint = if (!isSystemInDarkTheme()) textColorLight else
            textColorDark,
        contentDescription = null,
        modifier = Modifier
            .clickable {
                onEventHandler.invoke(ActiveGameEvent.OnNewGameClicked)
            }
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .height(36.dp)
    )
}


@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun GameContent(
    onEventHandler: (ActiveGameEvent) -> Unit,
    viewModel: ActiveGameViewModel
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints {
        val screenWidth = with(LocalDensity.current) {
            constraints.maxWidth.toDp()
        }

        val screenHeight = with(LocalDensity.current) {
            constraints.maxHeight.toDp()
        }

        val boardSize = if (isLandscape) {
            (screenHeight * 0.9f)
        } else{
            (screenWidth * 0.95f)
        }

        val margin = with(LocalDensity.current) {
            when {
                constraints.maxHeight.toDp().value < 500 -> 20
                constraints.maxHeight.toDp().value < 550 -> 8
                else -> 0
            }
        }

        if (isLandscape) {
            // --- LAYOUT ORIZZONTALE (LANDSCAPE) ---
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp), // Padding generale per il layout orizzontale
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colonna sinistra per la board
                Column(
                    modifier = Modifier.weight(1f), // Occupare metà dello spazio disponibile
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        Modifier
                            .size(boardSize) // Usa la dimensione calcolata per la board
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.inversePrimary
                            )
                    ) {
                        SudokuBoard(
                            onEventHandler,
                            viewModel,
                            boardSize
                        )
                    }
                }

                Spacer(Modifier.width(16.dp)) // Spazio tra la board e i controlli

                // Colonna destra per timer, difficoltà e input buttons
                Column(
                    modifier = Modifier.weight(0.7f), // Occupare l'altra metà (o meno, se preferisci)
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Timer e Difficoltà in una riga
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimerText(viewModel)

                        // Difficoltà
                        Row(
                            modifier = Modifier.wrapContentSize()
                        ) {
                            (0..viewModel.difficulty.ordinal).forEach {
                                Icon(
                                    contentDescription = stringResource(R.string.difficulty),
                                    imageVector = Icons.Filled.Star,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Bottoni di input
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        InputButtonRow(
                            (0..4).toList(),
                            onEventHandler
                        )
                        InputButtonRow(
                            (5..9).toList(),
                            onEventHandler
                        )
                    }
                }
            }
        } else {
            // --- LAYOUT VERTICALE (PORTRAIT) ---
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (board, timer, diff, inputs) = createRefs()

                Box(
                    Modifier
                        .constrainAs(board) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                        .background(MaterialTheme.colorScheme.surface)
                        .size(boardSize)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.inversePrimary
                        )
                ) {
                    SudokuBoard(
                        onEventHandler,
                        viewModel,
                        boardSize
                    )
                }

                Row(
                    Modifier
                        .wrapContentSize()
                        .constrainAs(diff) {
                            top.linkTo(board.bottom)
                            end.linkTo(parent.end)
                        }
                ) {
                    (0..viewModel.difficulty.ordinal).forEach {
                        Icon(
                            contentDescription = stringResource(R.string.difficulty),
                            imageVector = Icons.Filled.Star,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(top = 4.dp)
                        )
                    }
                }

                Box(
                    Modifier
                        .wrapContentSize()
                        .constrainAs(timer) {
                            top.linkTo(board.bottom)
                            start.linkTo(parent.start)
                        }
                        .padding(start = 16.dp)
                ) {
                    TimerText(viewModel)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .constrainAs(inputs) {
                            top.linkTo(timer.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    InputButtonRow(
                        (0..4).toList(),
                        onEventHandler
                    )

                    InputButtonRow(
                        (5..9).toList(),
                        onEventHandler
                    )
                }
            }
        }
    }
}

@Composable
fun InputButtonRow(numbers: List<Int>, onEventHandler: (ActiveGameEvent) -> Unit) {
    Row {
        numbers.forEach {
            SudokuInputButton(
                onEventHandler,
                it
            )
        }
    }

    Spacer(Modifier.size(2.dp))
}

@Composable
fun SudokuInputButton(onEventHandler: (ActiveGameEvent) -> Unit, number: Int) {
    OutlinedButton(
        onClick = { onEventHandler.invoke(ActiveGameEvent.onInput(number))},
        modifier = Modifier
            .requiredSize(56.dp)
            .padding(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimary)
    ) {
        Text(
            text = number.toString(),
            style = inputButton.copy(color = MaterialTheme.colorScheme.onPrimary),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun GameCompleteContent(timerState: Long, isNewRecordState: Boolean) {

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // --- LAYOUT ORIZZONTALE (LANDSCAPE) ---
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    contentDescription = stringResource(R.string.game_complete),
                    imageVector = Icons.Filled.EmojiEvents,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.size(128.dp)
                )

                if (isNewRecordState) {
                    Image(
                        contentDescription = null,
                        imageVector = Icons.Filled.Star,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.size(128.dp)
                    )
                }
            }

            Spacer(Modifier.width(24.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(), 
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.total_time),
                    style = newGameSubtitle.copy(
                        color = MaterialTheme.colorScheme.secondary
                    )
                )

                Text(
                    text = timerState.toTime(),
                    style = newGameSubtitle.copy(
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    } else {
        // --- LAYOUT VERTICALE (PORTRAIT) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    contentDescription = stringResource(R.string.game_complete),
                    imageVector = Icons.Filled.EmojiEvents,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.size(128.dp)
                )

                if (isNewRecordState) Image(
                    contentDescription = null,
                    imageVector = Icons.Filled.Star,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.size(128.dp)
                )
            }

            Text(
                text = stringResource(R.string.total_time),
                style = newGameSubtitle.copy(
                    color = MaterialTheme.colorScheme.secondary
                )
            )

            Text(
                text = timerState.toTime(),
                style = newGameSubtitle.copy(
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Normal
                )
            )
        }
    }
}

@Composable
fun TimerText(viewModel: ActiveGameViewModel) {
    var timerState by remember {
        mutableStateOf("")
    }

    viewModel.subTimerState = {
        timerState = it.toTime()
    }

    Text(
        modifier = Modifier.requiredHeight(36.dp),
        text = timerState,
        style = activeGameSubtitle.copy(
            color = MaterialTheme.colorScheme.secondary
        )
    )
}

@Composable
fun SudokuBoard(
    onEventHandler: (ActiveGameEvent) -> Unit,
    viewModel: ActiveGameViewModel,
    size: Dp
) {
    val tileOffset = size.value / 9

    var boardState by remember {
        mutableStateOf(viewModel.boardState, neverEqualPolicy())
    }

    viewModel.subBoardState = {
        boardState = it
    }

    SudokuTextFields(
        onEventHandler,
        tileOffset,
        boardState
    )

    BoardGrid(
        tileOffset
    )
}

@Composable
fun BoardGrid(tileOffset: Float) {
    (1 until 9).forEach {
        val width = if ( it % 9 == 0) 3.dp
        else 1.dp
        Divider(
            color = MaterialTheme.colorScheme.inversePrimary,
            modifier = Modifier
                .absoluteOffset((tileOffset * it).dp, 0.dp)
                .fillMaxHeight()
                .width(width)
        )

        val height = if ( it % 9 == 0) 3.dp
        else 1.dp
        Divider(
            color = MaterialTheme.colorScheme.inversePrimary,
            modifier = Modifier
                .absoluteOffset((tileOffset * it).dp, 0.dp)
                .fillMaxWidth()
                .height(height)
        )
    }
}

@Composable
fun SudokuTextFields(onEventHandler: (ActiveGameEvent) -> Unit, tileOffset: Float, boardState: HashMap<Int, SudokuTile>
) {
    boardState.values.forEach { tile ->
        var text = tile.value.toString()

        if (!tile.readOnly) {
            if (text == "0") text = ""

            Text (
                text = text,
                style = mutableSudokuSquare(tileOffset).copy(
                    color = if (!isSystemInDarkTheme()) userInputtedNumberLight
                    else userInputtedNumberDark
                ),
                modifier = Modifier
                    .absoluteOffset(
                        (tileOffset * (tile.x - 1)).dp,
                        (tileOffset * (tile.y - 1)).dp
                    )
                    .size(tileOffset.dp)
                    .background(
                        if (tile.hasFocus) MaterialTheme.colorScheme.onPrimary.copy(alpha = .25f)
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable {
                        onEventHandler.invoke(
                            ActiveGameEvent.onTileFocused(tile.x, tile.y)
                        )
                    }
            )
        } else {
            Text(
                text = text,
                style = readOnlySudokuSquare(
                    tileOffset
                ),
                modifier = Modifier
                    .absoluteOffset(
                        (tileOffset * (tile.x - 1)).dp,
                        (tileOffset * (tile.y - 1)).dp
                    )
                    .size(tileOffset.dp)
            )
        }
    }
}