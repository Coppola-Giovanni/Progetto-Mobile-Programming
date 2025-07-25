package com.sudokuMaster.ui.activegame

import com.sudokuMaster.logic.isComplete
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
import com.sudokuMaster.logic.getSmartHint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import java.util.LinkedList

data class SudokuTile(
    val x: Int,
    val y: Int,
    var value: Int,
    var hasFocus: Boolean,
    var readOnly: Boolean,
    var notes: Set<Int> = emptySet(),
    val isInvalid: Boolean = false
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

    private val _selectedTile = MutableStateFlow(SudokuTile(0, 0, 0, true, true))
    val selectedTile: StateFlow<SudokuTile> = _selectedTile.asStateFlow()

    private val _timerState = MutableStateFlow(0L)
    val timerState: StateFlow<Long> = _timerState.asStateFlow()

    private val _isSolved = MutableStateFlow(false)
    val isSolved: StateFlow<Boolean> = _isSolved.asStateFlow()

    private val _isNewRecord = MutableStateFlow(false)
    val isNewRecord: StateFlow<Boolean> = _isNewRecord.asStateFlow()

    private var _currentPuzzleId = MutableStateFlow(0L)

    private val _currentDifficulty = MutableStateFlow(DifficultyLevel.MEDIUM)
    val currentDifficulty: StateFlow<DifficultyLevel> = _currentDifficulty.asStateFlow()

    private val _hasInvalidTiles = MutableStateFlow(false)
    val hasInvalidTiles: StateFlow<Boolean> = _hasInvalidTiles.asStateFlow()

    private val _isNotesMode = MutableStateFlow(false)
    val isNotesMode: StateFlow<Boolean> = _isNotesMode.asStateFlow()

    val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val sudokuTiles: StateFlow<List<SudokuTile>> = _sudokuPuzzle.map { puzzle ->
        puzzle?.let {
            val tiles = it.currentGraph.values.flatten().map { node ->
                val tempBoardForCheck = puzzle.currentGraph.values.flatten().associateBy { getHash(it.x, it.y) }.toMutableMap()
                val isNodeValid = isValidMove(tempBoardForCheck, node.x, node.y, node.color)
                SudokuTile(
                    x = node.x,
                    y = node.y,
                    value = node.color,
                    hasFocus = false,
                    readOnly = node.readOnly,
                    notes = node.notes,
                    isInvalid = !isNodeValid && node.color != 0
                )
            }.map { tile ->
                tile.copy(hasFocus = (_selectedTile.value.x == tile.x && _selectedTile.value.y == tile.y))
            }
            tiles
        } ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


    private var gameJob: Job? = null
    private var timerJob: Job? = null

    init {
        if (initialGameType == "new") {
            loadNewGame()
        } else {
            loadExistingGame()
        }
    }

    fun onEvent(event: ActiveGameEvent) {
        when (event) {
            ActiveGameEvent.OnStart -> {
                if (_activeGameScreenState.value == ActiveGameScreenState.ACTIVE && timerJob?.isActive != true) {
                    startTimer()
                }
            }
            ActiveGameEvent.OnStop -> {
                stopTimer()
                saveCurrentGameSession()
            }
            ActiveGameEvent.OnNewGameClicked -> {
                createNewGame()
            }
            is ActiveGameEvent.OnInput -> {
                if (_isNotesMode.value) {
                    updateNotes(event.input)
                } else {
                    updateGameData(event.input)
                }
            }
            is ActiveGameEvent.OnNoteInput -> { // Gestisce specificamente l'input delle note
                updateNotes(event.input)
            }
            is ActiveGameEvent.OnTileFocused -> {
                _selectedTile.value = _selectedTile.value.copy(
                    x = event.x,
                    y = event.y,
                    hasFocus = true
                )
            }
            ActiveGameEvent.OnSuggestMoveClicked -> onSuggestMoveClicked()

            ActiveGameEvent.OnToggleNotesMode -> { // Alterna la modalità note
                _isNotesMode.value = !_isNotesMode.value
            }
        }
    }

    private fun loadNewGame() {
        _activeGameScreenState.value = ActiveGameScreenState.LOADING
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            try {
                val userPreferences = userPreferencesFlow
                    .filterNotNull()
                    .first()
                val preferredDifficulty = userPreferences.defaultDifficulty

                gameRepository.createNewGameAndSave(
                    difficulty = preferredDifficulty,
                    onSuccess = { gameSession ->
                        _currentPuzzleId.value = gameSession.id
                        val puzzle = gameSession.toSudokuPuzzle()
                        _sudokuPuzzle.value = puzzle
                        _selectedTile.value = SudokuTile(0, 0, 0, true, true)
                        _timerState.value = gameSession.durationSeconds ?: 0L
                        _isSolved.value = gameSession.isSolved
                        _currentDifficulty.value = gameSession.difficulty.toDifficultyLevel()
                        _activeGameScreenState.value = ActiveGameScreenState.ACTIVE
                        startTimer()
                    },
                    onError = { _ ->
                        _activeGameScreenState.value = ActiveGameScreenState.ERROR
                    }
                )
            } catch (e: Exception) {
                _activeGameScreenState.value = ActiveGameScreenState.ERROR
            }
        }
    }

    private fun loadExistingGame() {
        _activeGameScreenState.value = ActiveGameScreenState.LOADING
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            try {
                gameRepository.getLatestUnfinishedGameSession(
                    onSuccess = { gameSession ->
                        _currentPuzzleId.value = gameSession.id
                        val puzzle = gameSession.toSudokuPuzzle()
                        _sudokuPuzzle.value = puzzle
                        _selectedTile.value = SudokuTile(0, 0, 0, true, true)
                        _timerState.value = gameSession.durationSeconds ?: 0L
                        _isSolved.value = gameSession.isSolved
                        _currentDifficulty.value = gameSession.difficulty.toDifficultyLevel()
                        _activeGameScreenState.value = ActiveGameScreenState.ACTIVE
                        startTimer()
                    },
                    onError = { throwable ->
                        createNewGame()
                    }
                )
            } catch (e: Exception) {
                createNewGame()
            }
        }
    }

    private fun createNewGame() {
        loadNewGame()
    }

    private fun updateGameData(input: Int) {
        _sudokuPuzzle.value?.let { currentPuzzle ->
            val updatedCurrentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()

            currentPuzzle.currentGraph.forEach { (row, nodes) ->
                updatedCurrentGraph[row] = LinkedList(nodes.map { node ->
                    if (node.x == selectedTile.value.x && node.y == selectedTile.value.y && !node.readOnly) {
                        // Quando si inserisce un valore definitivo, le note devono essere cancellate per quella cella
                        node.copy(color = input, notes = emptySet())
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
                _activeGameScreenState.value = ActiveGameScreenState.COMPLETE
            } else {
                saveCurrentGameSession()
            }
        }
    }

    private fun updateNotes(input: Int) {
        _sudokuPuzzle.value?.let { currentPuzzle ->
            val updatedCurrentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()
            val focusedTile = _selectedTile.value

            currentPuzzle.currentGraph.forEach { (row, nodes) ->
                updatedCurrentGraph[row] = LinkedList(nodes.map { node ->
                    if (node.x == focusedTile.x && node.y == focusedTile.y && !node.readOnly) {
                        // Una cella con un valore definitivo (color != 0) non può avere note.
                        if (node.color == 0) {
                            val newNotes = if (node.notes.contains(input)) {
                                node.notes - input
                            } else {
                                (node.notes + input).filter { it in 1..9 }.toSet() // Aggiungi la nota se non presente, filtra numeri validi
                            }
                            node.copy(notes = newNotes)
                        } else {
                            node.copy() // Non modificare se la cella ha un valore definitivo
                        }
                    } else {
                        node.copy()
                    }
                })
            }

            val updatedPuzzle = currentPuzzle.copy(currentGraph = updatedCurrentGraph)
            _sudokuPuzzle.value = updatedPuzzle
            saveCurrentGameSession() // Salva lo stato del gioco dopo aver aggiornato le note
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
    }

    private fun stopTimer() {
        timerJob?.cancel()
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
                    },
                    onError = { throwable ->
                    }
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        gameJob?.cancel()
    }

    private fun onSuggestMoveClicked() = viewModelScope.launch {
        val currentPuzzle = _sudokuPuzzle.value ?: return@launch

        val suggestedNodeAndValue = currentPuzzle.getSmartHint() ?: return@launch

        val (suggestedNode, suggestedValue) = suggestedNodeAndValue

        // Aggiorna la griglia con il suggerimento
        val updatedNodesMap = LinkedHashMap<Int, LinkedList<SudokuNode>>()
        currentPuzzle.currentGraph.forEach { (row, nodes) ->
            updatedNodesMap[row] = LinkedList(nodes.map { node ->
                if (node.x == suggestedNode.x && node.y == suggestedNode.y) {
                    node.copy(color = suggestedValue, notes = emptySet())
                } else {
                    node.copy()
                }
            })
        }
        _sudokuPuzzle.value = currentPuzzle.copy(currentGraph = updatedNodesMap)

        // Aggiorna la tile selezionata per evidenziare la mossa suggerita
        _selectedTile.value = SudokuTile(
            suggestedNode.x,
            suggestedNode.y,
            suggestedValue,
            true,
            suggestedNode.readOnly, // Usa il valore readOnly del nodo suggerito
            notes = emptySet()
        )

        // Controlla se il puzzle è completo e salva lo stato
        _sudokuPuzzle.value?.let { updatedPuzzle ->
            if (updatedPuzzle.isComplete()) {
                _isSolved.value = true
                stopTimer()
                saveCurrentGameSession(isSolved = true)
                _activeGameScreenState.value = ActiveGameScreenState.COMPLETE
            } else {
                saveCurrentGameSession()
            }
        }
    }

    fun setHasInvalidTiles(value: Boolean) {
        _hasInvalidTiles.value = value
    }

    private fun isValidMove(
        board: MutableMap<Int, SudokuNode>,
        row: Int,
        col: Int,
        num: Int,
    ): Boolean {
        // 1. Controlla la riga
        for (c in 0 until 9) {
            val node = board[getHash(row, c)]
            if (c != col && node != null && node.color == num) {
                return false
            }
        }

        // 2. Controlla la collonna
        for (r in 0 until 9) {
            val node = board[getHash(r, col)]
            if (r != row && node != null && node.color == num) {
                return false
            }
        }

        // 3. Controlla il bloco 3x3
        val subgridSize = 3
        val startRow = (row / subgridSize) * subgridSize
        val startCol = (col / subgridSize) * subgridSize

        for (r in startRow until startRow + subgridSize) {
            for (c in startCol until startCol + subgridSize) {
                val node = board[getHash(r, c)]
                if ((r != row || c != col) && node != null && node.color == num) {
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