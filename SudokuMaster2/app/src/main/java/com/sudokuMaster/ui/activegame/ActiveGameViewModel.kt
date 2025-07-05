package com.sudokuMaster.ui.activegame


import com.sudokuMaster.logic.isComplete
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuMaster.common.toDifficultyLevel
import com.sudokuMaster.common.toGameSession
import com.sudokuMaster.common.toSudokuPuzzle
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.domain.GameRepositoryInterface
import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import com.sudokuMaster.domain.getHash
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map // Import required for .map
import kotlinx.coroutines.flow.SharingStarted // Import required for .stateIn
import kotlinx.coroutines.flow.stateIn // Import required for .stateIn
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.concurrent.TimeUnit

// ... (SudokuTile e ActiveGameScreenState come forniti) ...

data class SudokuTile(
    val x: Int,
    val y: Int,
    var value: Int,
    var hasFocus: Boolean,
    var readOnly: Boolean
)

enum class ActiveGameScreenState {
    LOADING,
    ACTIVE,
    COMPLETE,
    ERROR
}

class ActiveGameViewModel(
    private val gameRepository: GameRepositoryInterface,
    private val userPreferencesRepository: UserPreferencesRepositoryInterface,
    private val initialGameType: String
) : ViewModel() {

    private val _activeGameScreenState = MutableStateFlow(ActiveGameScreenState.LOADING)
    val activeGameScreenState: StateFlow<ActiveGameScreenState> = _activeGameScreenState.asStateFlow()

    private val _sudokuPuzzle = MutableStateFlow<SudokuPuzzle?>(null)
    val sudokuPuzzle: StateFlow<SudokuPuzzle?> = _sudokuPuzzle.asStateFlow()

    private val _selectedTile = MutableStateFlow(SudokuTile(0, 0, 0, true, true))
    val selectedTile: StateFlow<SudokuTile> = _selectedTile.asStateFlow()

    private val _timerState = MutableStateFlow(0L)
    val timerState: StateFlow<Long> = _timerState.asStateFlow()

    private val _isSolved = MutableStateFlow(false)
    val isSolved: StateFlow<Boolean> = _isSolved.asStateFlow()

    private val _isNewRecord = MutableStateFlow(false)
    val isNewRecord: StateFlow<Boolean> = _isNewRecord.asStateFlow()

    private var _currentPuzzleId = MutableStateFlow(0L)
    val currentPuzzleId: StateFlow<Long> = _currentPuzzleId.asStateFlow()

    // ADDED: StateFlow per la difficoltà corrente
    private val _currentDifficulty = MutableStateFlow(DifficultyLevel.MEDIUM) // Default iniziale
    val currentDifficulty: StateFlow<DifficultyLevel> = _currentDifficulty.asStateFlow()

    // ADDED: StateFlow per la lista di SudokuTile per la UI
    val sudokuTiles: StateFlow<List<SudokuTile>> = _sudokuPuzzle.map { puzzle ->
        puzzle?.let {
            it.currentGraph.values.flatten().map { node ->
                SudokuTile(
                    x = node.x,
                    y = node.y,
                    value = node.color,
                    hasFocus = false, // La logica del focus è gestita dalla UI con selectedTile
                    readOnly = node.readOnly
                )
            }
        } ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Mantiene la flow attiva per 5 secondi dopo che nessun collector è attivo
        initialValue = emptyList()
    )


    private var gameJob: Job? = null
    private var timerJob: Job? = null

    init {
        Log.d("ActiveGameViewModel", "ViewModel init: initialGameType = $initialGameType")
        if (initialGameType == "new") {
            Log.d("ActiveGameViewModel", "init: Calling loadNewGame().")
            loadNewGame()
        } else { // "continue" o qualsiasi altra stringa
            Log.d("ActiveGameViewModel", "init: Calling loadExistingGame().")
            loadExistingGame()
        }
    }

    fun onEvent(event: ActiveGameEvent) {
        when (event) {
            ActiveGameEvent.OnStart -> {
                Log.d("ActiveGameViewModel", "Event: OnStart received. Current state: ${_activeGameScreenState.value}")
                if (_activeGameScreenState.value == ActiveGameScreenState.ACTIVE && timerJob?.isActive != true) {
                    startTimer()
                }
            }
            ActiveGameEvent.OnStop -> {
                Log.d("ActiveGameViewModel", "Event: OnStop received. Current state: ${_activeGameScreenState.value}")
                stopTimer()
                saveCurrentGameSession()
            }
            ActiveGameEvent.OnNewGameClicked -> {
                Log.d("ActiveGameViewModel", "Event: OnNewGameClicked received. Starting new game...")
                createNewGame()
            }
            is ActiveGameEvent.onInput -> {
                Log.d("ActiveGameViewModel", "Event: OnInput received: input = ${event.input}, tile = (${selectedTile.value.x}, ${selectedTile.value.y})")
                updateGameData(event.input)
            }
            is ActiveGameEvent.onTileFocused -> {
                Log.d("ActiveGameViewModel", "Event: OnTileFocused received: x = ${event.x}, y = ${event.y}")
                _selectedTile.value = _selectedTile.value.copy(
                    x = event.x,
                    y = event.y,
                    hasFocus = true // Questo ha senso per il ViewModel, ma la UI usa selectedTile per il render
                )
            }

            ActiveGameEvent.OnSuggestMoveClicked -> onSuggestMoveClicked()
        }
    }

    private fun loadNewGame() {
        Log.d("ActiveGameViewModel", "loadNewGame: Attempting to create new game.")
        _activeGameScreenState.value = ActiveGameScreenState.LOADING
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            try {
                val userPrefs = userPreferencesRepository.getUserPreferences().firstOrNull()

                val defaultDifficultyString = userPrefs?.defaultDifficulty
                // NUOVA LOGICA: Trova DifficultyLevel confrontando ignorando il case
                val defaultDifficulty = if (defaultDifficultyString != null) {
                    var foundDifficulty: DifficultyLevel? = null
                    for (level in DifficultyLevel.values()) {
                        if (level.name.equals(defaultDifficultyString.toString(), ignoreCase = true)) {
                            foundDifficulty = level
                            break
                        }
                    }
                    foundDifficulty ?: DifficultyLevel.MEDIUM // Se non trovato, usa MEDIUM
                } else {
                    DifficultyLevel.MEDIUM // Se la stringa è nulla, usa MEDIUM
                }
                // FINE NUOVA LOGICA

                Log.d("ActiveGameViewModel", "loadNewGame: Fetched default difficulty: $defaultDifficulty")

                gameRepository.createNewGameAndSave(
                    difficulty = defaultDifficulty,
                    onSuccess = { gameSession ->
                        Log.d("ActiveGameViewModel", "loadNewGame: Game created successfully, ID: ${gameSession.id}")
                        _currentPuzzleId.value = gameSession.id
                        val puzzle = gameSession.toSudokuPuzzle()
                        _sudokuPuzzle.value = puzzle
                        _selectedTile.value = SudokuTile(0, 0, 0, true, true)
                        _timerState.value = gameSession.durationSeconds ?: 0L
                        _isSolved.value = gameSession.isSolved
                        _currentDifficulty.value = defaultDifficulty
                        _activeGameScreenState.value = ActiveGameScreenState.ACTIVE
                        startTimer()
                        Log.d("ActiveGameViewModel", "loadNewGame: State changed to ACTIVE.")
                    },
                    onError = { throwable ->
                        Log.e("ActiveGameViewModel", "loadNewGame: Error creating new game: ${throwable.message}", throwable)
                        _activeGameScreenState.value = ActiveGameScreenState.ERROR
                    }
                )
            } catch (e: Exception) {
                Log.e("ActiveGameViewModel", "loadNewGame: Unexpected error during new game load: ${e.message}", e)
                _activeGameScreenState.value = ActiveGameScreenState.ERROR
            }
        }
    }


    private fun loadExistingGame() {
        Log.d("ActiveGameViewModel", "loadExistingGame: Attempting to load existing game.")
        _activeGameScreenState.value = ActiveGameScreenState.LOADING
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            try {
                gameRepository.getLatestUnfinishedGameSession(
                    onSuccess = { gameSession ->
                        Log.d("ActiveGameViewModel", "loadExistingGame: Existing game loaded successfully, ID: ${gameSession.id}")
                        _currentPuzzleId.value = gameSession.id
                        val puzzle = gameSession.toSudokuPuzzle()
                        _sudokuPuzzle.value = puzzle
                        _selectedTile.value = SudokuTile(0, 0, 0, true, true)
                        _timerState.value = gameSession.durationSeconds ?: 0L
                        _isSolved.value = gameSession.isSolved
                        // ASSUMPTION: gameSession ha una proprietà 'difficulty' che è una String o un DifficultyLevel
                        _currentDifficulty.value = gameSession.difficulty.toDifficultyLevel() // AGGIUNTO: Aggiorna la difficoltà
                        _activeGameScreenState.value = ActiveGameScreenState.ACTIVE
                        startTimer()
                        Log.d("ActiveGameViewModel", "loadExistingGame: State changed to ACTIVE.")
                    },
                    onError = { throwable ->
                        Log.e("ActiveGameViewModel", "loadExistingGame: Error loading existing game: ${throwable.message}. Attempting to create new game as fallback.", throwable)
                        createNewGame()
                    }
                )
            } catch (e: Exception) {
                Log.e("ActiveGameViewModel", "loadExistingGame: Unexpected error during existing game load: ${e.message}. Attempting to create new game as fallback.", e)
                createNewGame()
            }
        }
    }

    private fun createNewGame() {
        Log.d("ActiveGameViewModel", "createNewGame: Called from event. Delegating to loadNewGame.")
        loadNewGame()
    }

    private fun updateGameData(input: Int) {
        Log.d("ActiveGameViewModel", "updateGameData: Updating tile (${selectedTile.value.x}, ${selectedTile.value.y}) with value $input")

        _sudokuPuzzle.value?.let { currentPuzzle ->
            val updatedCurrentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()
            currentPuzzle.currentGraph.forEach { (row, nodes) ->
                updatedCurrentGraph[row] = LinkedList(nodes.map { node ->
                    if (node.x == selectedTile.value.x && node.y == selectedTile.value.y && !node.readOnly) {
                        node.copy(color = input)
                    } else {
                        node.copy()
                    }
                })
            }

            val updatedPuzzle = currentPuzzle.copy(currentGraph = updatedCurrentGraph)
            _sudokuPuzzle.value = updatedPuzzle

            if (updatedPuzzle.isComplete()) {
                _isSolved.value = true
                stopTimer()
                saveCurrentGameSession(isSolved = true)
                Log.d("ActiveGameViewModel", "Puzzle solved! Transitioning to COMPLETE state.")
                _activeGameScreenState.value = ActiveGameScreenState.COMPLETE
            } else {
                saveCurrentGameSession()
                Log.d("ActiveGameViewModel", "Puzzle updated, not yet solved. Saving progress.")
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _timerState.value = _timerState.value + 1
            }
        }
        Log.d("ActiveGameViewModel", "Timer started.")
    }

    private fun stopTimer() {
        timerJob?.cancel()
        Log.d("ActiveGameViewModel", "Timer stopped.")
    }

    private fun saveCurrentGameSession(isSolved: Boolean = false) {
        val puzzleToSave = _sudokuPuzzle.value
        val currentDuration = _timerState.value
        val puzzleId = _currentPuzzleId.value

        if (puzzleToSave != null && puzzleId != 0L) {
            val gameSession = puzzleToSave.toGameSession(
                existingId = puzzleId,
                isSolved = isSolved,
                score = 0
            ).copy(durationSeconds = currentDuration)

            viewModelScope.launch {
                gameRepository.updateGameSession(
                    gameSession = gameSession,
                    onSuccess = { updatedSession ->
                        Log.d("ActiveGameViewModel", "Game session saved successfully. ID: ${updatedSession.id}")
                    },
                    onError = { throwable ->
                        Log.e("ActiveGameViewModel", "Error saving game session: ${throwable.message}", throwable)
                    }
                )
            }
        } else {
            Log.w("ActiveGameViewModel", "Cannot save game session: puzzle is null or puzzleId is 0.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        gameJob?.cancel()
        Log.d("ActiveGameViewModel", "ViewModel onCleared.")
    }

    private fun onSuggestMoveClicked() = viewModelScope.launch {
        // Ensure _sudokuPuzzle.value is not null before proceeding
        val currentPuzzle = _sudokuPuzzle.value ?: run {
            Log.e("SudokuDebug", "onSuggestMoveClicked: _sudokuPuzzle is null. Cannot suggest move.")
            return@launch
        }

        // Get a mutable map of the current graph for easier manipulation
        val currentBoard = currentPuzzle.currentGraph.values.flatten().associateBy { getHash(it.x, it.y) }.toMutableMap()
        val boundary = 9 // Assuming a 9x9 grid

        Log.d("SudokuDebug", "--- SUGGEST MOVE CLICKED ---")
        Log.d("SudokuDebug", "Current board state (values only) as seen by onSuggestMoveClicked:")
        for (y in 0 until boundary) {
            val rowValues = (0 until boundary).map { x ->
                currentBoard[getHash(x, y)]?.color ?: 0 // Use .color for SudokuNode
            }
            Log.d("SudokuDebug", rowValues.joinToString(separator = ", "))
        }

        var suggestedX: Int? = null
        var suggestedY: Int? = null
        var suggestedValue: Int? = null

        // Get the currently focused tile from _selectedTile
        val focusedTile = _selectedTile.value

        // Check if the focused tile is empty and mutable
        if (focusedTile.value == 0 && !focusedTile.readOnly) {
            Log.d("SudokuDebug", "Focused on empty mutable tile: (${focusedTile.x}, ${focusedTile.y})")
            for (num in 1..boundary) {
                // Create a temporary board for validity check. It's crucial to pass a copy.
                val tempBoardForCheck = currentBoard.toMutableMap()
                val tempNode = SudokuNode(focusedTile.x, focusedTile.y, num, focusedTile.readOnly)
                tempBoardForCheck[getHash(focusedTile.x, focusedTile.y)] = tempNode

                if (isValidMove(tempBoardForCheck, focusedTile.x, focusedTile.y, num, boundary)) {
                    suggestedX = focusedTile.x
                    suggestedY = focusedTile.y
                    suggestedValue = num
                    Log.d("SudokuDebug", "  Found valid suggestion $num for focused tile.")
                    break // Found a suggestion for the focused tile
                }
            }
        }

        // If no move was found for the focused cell, or if there wasn't an empty/mutable focused cell,
        // search for the first empty and mutable cell.
        if (suggestedValue == null) {
            Log.d("SudokuDebug", "No suggestion for focused tile, searching first empty mutable tile.")
            outerLoop@ for (y in 0 until boundary) {
                for (x in 0 until boundary) {
                    val node = currentBoard[getHash(x, y)]
                    if (node != null && node.color == 0 && !node.readOnly) { // Empty and mutable cell
                        Log.d("SudokuDebug", "  Found empty mutable tile: ($x, $y)")
                        for (num in 1..boundary) { // Try numbers from 1 to 9
                            // Create a temporary board for validity check
                            val tempBoardForCheck = currentBoard.toMutableMap()
                            val tempNode = SudokuNode(x, y, num, node.readOnly)
                            tempBoardForCheck[getHash(x, y)] = tempNode

                            if (isValidMove(tempBoardForCheck, x, y, num, boundary)) {
                                suggestedX = x
                                suggestedY = y
                                suggestedValue = num
                                Log.d("SudokuDebug", "    Found valid suggestion $num for ($x, $y).")
                                break@outerLoop // Found a suggestion, exit all loops
                            }
                        }
                    }
                }
            }
        }

        if (suggestedX != null && suggestedY != null && suggestedValue != null) {
            Log.d("SudokuDebug", "Suggested move: Value $suggestedValue at ($suggestedX, $suggestedY)")

            // Create a new SudokuPuzzle with the updated tile
            val updatedNodesMap = LinkedHashMap<Int, LinkedList<SudokuNode>>()
            currentPuzzle.currentGraph.forEach { (row, nodes) ->
                updatedNodesMap[row] = LinkedList(nodes.map { node ->
                    if (node.x == suggestedX && node.y == suggestedY) {
                        node.copy(color = suggestedValue)
                    } else {
                        node.copy()
                    }
                })
            }
            _sudokuPuzzle.value = currentPuzzle.copy(currentGraph = updatedNodesMap)

            // Update _selectedTile to reflect the newly focused tile
            _selectedTile.value = SudokuTile(suggestedX, suggestedY, suggestedValue, true, false) // The suggested tile is never readOnly

            // Check if the puzzle is complete after the suggestion
            _sudokuPuzzle.value?.let { updatedPuzzle ->
                if (updatedPuzzle.isComplete()) {
                    _isSolved.value = true
                    stopTimer()
                    saveCurrentGameSession(isSolved = true)
                    Log.d("ActiveGameViewModel", "Puzzle solved after suggestion! Transitioning to COMPLETE state.")
                    _activeGameScreenState.value = ActiveGameScreenState.COMPLETE
                } else {
                    saveCurrentGameSession()
                    Log.d("ActiveGameViewModel", "Puzzle updated with suggestion, not yet solved. Saving progress.")
                }
            }

        } else {
            Log.d("SudokuDebug", "No valid move found to suggest.")
            // You might want to show a Toast or a message to the user here.
            // This would typically be handled via a One-Shot event in a Compose app.
        }
    }


    private fun isValidMove(
        board: MutableMap<Int, SudokuNode>, // Changed to MutableMap<Int, SudokuNode> to match usage
        row: Int,
        col: Int,
        num: Int,
        boundary: Int
    ): Boolean {
        // 1. Check the row
        for (c in 0 until boundary) {
            val node = board[getHash(row, c)] // Use SudokuNode here
            // Do not check the cell itself
            if (c != col && node != null && node.color == num) { // Use .color for SudokuNode
                return false
            }
        }

        // 2. Check the column
        for (r in 0 until boundary) {
            val node = board[getHash(r, col)] // Use SudokuNode here
            // Do not check the cell itself
            if (r != row && node != null && node.color == num) { // Use .color for SudokuNode
                return false
            }
        }

        // 3. Check the 3x3 block
        val subgridSize = Math.sqrt(boundary.toDouble()).toInt()
        val startRow = (row / subgridSize) * subgridSize
        val startCol = (col / subgridSize) * subgridSize

        for (r in startRow until startRow + subgridSize) {
            for (c in startCol until startCol + subgridSize) {
                val node = board[getHash(r, c)] // Use SudokuNode here
                // Do not check the cell itself
                if ((r != row || c != col) && node != null && node.color == num) { // Use .color for SudokuNode
                    return false
                }
            }
        }
        return true
    }

}

class ActiveGameViewModelFactory(
    private val gameRepository: GameRepositoryInterface,
    private val userPreferencesRepository: UserPreferencesRepositoryInterface,
    private val initialGameType: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveGameViewModel::class.java)) {
            return ActiveGameViewModel(gameRepository, userPreferencesRepository, initialGameType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}