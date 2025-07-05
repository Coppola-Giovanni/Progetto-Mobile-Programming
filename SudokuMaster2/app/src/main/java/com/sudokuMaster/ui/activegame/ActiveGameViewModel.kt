package com.sudokuMaster.ui.activegame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.LinkedList

data class SudokuTile(
    val x: Int,
    val y: Int,
    var value: Int,
    var hasFocus: Boolean,
    var readOnly: Boolean
)


// Stato generale della schermata (Loading, Active, Complete)
enum class ActiveGameScreenState {
    LOADING,
    ACTIVE,
    COMPLETE,
    ERROR
}

// ViewModel per la schermata di gioco attiva
class ActiveGameViewModel(
    private val gameRepository: GameRepositoryInterface,
    private val userPreferencesRepository: UserPreferencesRepositoryInterface
) : ViewModel() {

    // --- STATO DELLA UI ---
    // Utilizziamo StateFlow per esporre lo stato osservabile alla UI
    private val _screenState = MutableStateFlow(ActiveGameScreenState.LOADING)
    val screenState: StateFlow<ActiveGameScreenState> = _screenState.asStateFlow()

    private val _boardState = MutableStateFlow<HashMap<Int, SudokuTile>>(HashMap())
    val boardState: StateFlow<HashMap<Int, SudokuTile>> = _boardState.asStateFlow()

    private val _timerState = MutableStateFlow(0L)
    val timerState: StateFlow<Long> = _timerState.asStateFlow()

    private val _difficulty = MutableStateFlow(DifficultyLevel.MEDIUM) // Default, sarà caricato dalle prefs
    val difficulty: StateFlow<DifficultyLevel> = _difficulty.asStateFlow()

    private val _isNewRecord = MutableStateFlow(false)
    val isNewRecord: StateFlow<Boolean> = _isNewRecord.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null) // Messaggio di errore per la UI
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()


    // Variabili interne
    private var timerJob: Job? = null
    private var currentPuzzleId: Long = -1L // ID del puzzle corrente

    init {
        // Avvia la logica di caricamento all'inizializzazione del ViewModel
        loadGame()
    }

    // --- EVENTI DALLA UI ---
    fun onEvent(event: ActiveGameEvent) {
        when (event) {
            is ActiveGameEvent.OnInput -> onInput(event.input)
            is ActiveGameEvent.OnTileFocused -> onTileFocused(event.x, event.y)
            ActiveGameEvent.OnNewGameClicked -> onNewGameClicked()
            ActiveGameEvent.OnStop -> onStop()
            ActiveGameEvent.OnStart -> { /* Già gestito in init o tramite loadGame */ }
        }
    }

    private fun onTileFocused(x: Int, y: Int) = viewModelScope.launch {
        val currentBoard = _boardState.value.toMutableMap()
        currentBoard.values.forEach { tile ->
            // Imposta a true solo la cella focalizzata, tutte le altre a false
            tile.hasFocus = (tile.x == x && tile.y == y)
        }
        _boardState.value = HashMap(currentBoard) // Aggiorna lo StateFlow con la nuova HashMap
    }


    private fun onInput(input: Int) = viewModelScope.launch {
        val focusedTile = _boardState.value.values.find { it.hasFocus }

        focusedTile?.let { tile ->
            // Non permettere di modificare celle readOnly
            if (tile.readOnly) return@launch

            val newBoardState = _boardState.value.toMutableMap()
            val updatedTile = tile.copy(value = input, hasFocus = false) // Copia per immutabilità
            newBoardState[getHash(tile.x, tile.y)] = updatedTile

            _boardState.value = HashMap(newBoardState) // Aggiorna lo StateFlow

            // Ora aggiorniamo il puzzle nel repository
            val elapsedTime = _timerState.value
            val puzzle = mapBoardStateToSudokuPuzzle(HashMap(newBoardState), _difficulty.value, elapsedTime)

            try {
                // Chiamata aggiornata senza callbacks
                val updatedGameSession = gameRepository.updateGameNodeAndSave(puzzle)

                if (updatedGameSession.isSolved) {
                    timerJob?.cancel()
                    _screenState.value = ActiveGameScreenState.COMPLETE // Imposta lo stato di gioco completo
                    val solveTimeMillis = updatedGameSession.durationSeconds?.let { it * 1000L }
                        ?: (updatedGameSession.endTimeMillis?.minus(updatedGameSession.startTimeMillis)
                            ?: 0L)
                    checkIfNewRecord(solveTimeMillis)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Errore durante l'aggiornamento del gioco: ${e.localizedMessage}"
                _screenState.value = ActiveGameScreenState.ERROR // Imposta lo stato di errore
                e.printStackTrace() // Per debug
            }
        }
    }

    private fun onNewGameClicked() = viewModelScope.launch {
        // Salva lo stato corrente se non è completo, poi naviga
        if (_screenState.value != ActiveGameScreenState.COMPLETE) {
            try {
                val currentBoard = _boardState.value
                val currentDifficulty = _difficulty.value
                val currentElapsedTime = _timerState.value

                val puzzleToSave = mapBoardStateToSudokuPuzzle(currentBoard, currentDifficulty, currentElapsedTime)

                // Chiamata aggiornata senza callbacks
                gameRepository.updateGameSession(puzzleToSave.toGameSession(currentPuzzleId))
                // Se il salvataggio ha successo, si può navigare o resetta lo stato.
                // La navigazione dovrebbe essere gestita dalla UI che osserva lo stato.
                resetGameStateAndPrepareForNewGame() // Prepara la ViewModel per un nuovo gioco
            } catch (e: Exception) {
                _errorMessage.value = "Errore nel salvataggio della partita corrente: ${e.localizedMessage}"
                _screenState.value = ActiveGameScreenState.ERROR
                e.printStackTrace()
                // Anche in caso di errore nel salvataggio, potresti voler permettere di iniziare una nuova partita
                resetGameStateAndPrepareForNewGame()
            }
        } else {
            resetGameStateAndPrepareForNewGame()
        }

}
    private fun resetGameStateAndPrepareForNewGame() {
        // Resetting the state to trigger UI re-render and potentially new game creation
        _boardState.value = HashMap()
        _timerState.value = 0L
        _difficulty.value = DifficultyLevel.MEDIUM // O il valore predefinito
        _isNewRecord.value = false
        _errorMessage.value = null
        currentPuzzleId = -1L
        timerJob?.cancel()
        _screenState.value = ActiveGameScreenState.LOADING // La UI dovrebbe reagire a questo per iniziare un nuovo caricamento
        loadGame() // Rilancia il processo di caricamento per una nuova partita
    }


    private fun navigateToNewGame() {
        // Questa logica di navigazione non può essere gestita direttamente dal ViewModel.
        // Il ViewModel aggiorna uno stato e l'Activity/Composable osserva quello stato
        // e innesca la navigazione.
        // Per ora, non facciamo nulla qui, l'ActiveGameActivity gestirà la navigazione.
        // _screenState.value = ActiveGameScreenState.LOADING // Potrebbe essere utile reset per la nuova partita
        // Poi l'Activity reindirizzerà.
        // Dobbiamo passare l'evento di "navigazione" alla UI
    }

    private fun onStop() {
        if (_screenState.value != ActiveGameScreenState.COMPLETE) {
            viewModelScope.launch {
                try {
                    val currentBoard = _boardState.value
                    val currentDifficulty = _difficulty.value
                    val currentElapsedTime = _timerState.value

                    val puzzleToSave = mapBoardStateToSudokuPuzzle(currentBoard, currentDifficulty, currentElapsedTime)

                    // Chiamata aggiornata senza callbacks
                    gameRepository.updateGameSession(puzzleToSave.toGameSession(currentPuzzleId))
                    timerJob?.cancel()
                } catch (e: Exception) {
                    _errorMessage.value = "Errore nel salvataggio della partita in pausa: ${e.localizedMessage}"
                    _screenState.value = ActiveGameScreenState.ERROR
                    e.printStackTrace()
                    timerJob?.cancel() // Annulla il timer anche in caso di errore
                }
            }
        } else {
            timerJob?.cancel()
        }
    }

    private fun loadGame() = viewModelScope.launch {
        _screenState.value = ActiveGameScreenState.LOADING
        _errorMessage.value = null // Resetta eventuali messaggi di errore precedenti
        try {
            // Tenta di ottenere l'ultima sessione non finita
            val gameSession = gameRepository.getLatestUnfinishedGameSession()

            if (gameSession != null) {
                // Ho trovato una sessione non finita
                currentPuzzleId = gameSession.id

                val puzzle = gameSession.toSudokuPuzzle()
                _difficulty.value = puzzle.difficulty
                _timerState.value = puzzle.elapsedTime
                _boardState.value = mapSudokuPuzzleToBoardState(puzzle)

                if (gameSession.isSolved) {
                    _screenState.value = ActiveGameScreenState.COMPLETE
                    _isNewRecord.value = false
                } else {
                    _screenState.value = ActiveGameScreenState.ACTIVE
                    startTimer()
                }
            } else {
                // Nessun gioco non finito, crea una nuova partita
                val settings = userPreferencesRepository.getUserPreferencesFlow().first() // Usa getUserPreferencesFlow() per ottenere le preferenze
                val defaultDifficulty = settings.defaultDifficulty

                // Chiamata aggiornata senza callbacks
                val newGameSession = gameRepository.createNewGameAndSave(defaultDifficulty)

                currentPuzzleId = newGameSession.id
                val newPuzzle = newGameSession.toSudokuPuzzle()
                _difficulty.value = newPuzzle.difficulty
                _timerState.value = newPuzzle.elapsedTime
                _boardState.value = mapSudokuPuzzleToBoardState(newPuzzle)
                _screenState.value = ActiveGameScreenState.ACTIVE
                startTimer()
            }
        } catch (e: IOException) {
            _errorMessage.value = "Errore di rete o di I/O durante il caricamento del gioco: ${e.localizedMessage}"
            _screenState.value = ActiveGameScreenState.ERROR
            e.printStackTrace()
        } catch (e: Exception) {
            _errorMessage.value = "Errore generico durante il caricamento del gioco: ${e.localizedMessage}"
            _screenState.value = ActiveGameScreenState.ERROR
            e.printStackTrace()
        }
    }

    private fun startTimer() {
        timerJob?.cancel() // Annulla qualsiasi timer precedente
        timerJob = viewModelScope.launch {
            while (_screenState.value == ActiveGameScreenState.ACTIVE) {
                delay(1000) // Aspetta 1 secondo
                _timerState.value += 1
            }
        }
    }

    private fun checkIfNewRecord(finalTimeMillis: Long) = viewModelScope.launch {
        try {
            val userStats = gameRepository.getUserStatistics().first()
            val currentDifficulty = _difficulty.value

            val isRecord = when (currentDifficulty) {
                DifficultyLevel.EASY -> userStats.bestSolveTimeEasyMillis?.let { finalTimeMillis < it } ?: false
                DifficultyLevel.MEDIUM -> userStats.bestSolveTimeMediumMillis?.let { finalTimeMillis < it } ?: false
                DifficultyLevel.HARD -> userStats.bestSolveTimeHardMillis?.let { finalTimeMillis < it } ?: false
                else -> false
            }

            _isNewRecord.value = isRecord
            _screenState.value = ActiveGameScreenState.COMPLETE
        } catch (e: Exception) {
            _errorMessage.value = "Errore nel calcolo del nuovo record: ${e.localizedMessage}"
            e.printStackTrace()
        }
    }

    // --- FUNZIONI DI MAPPATURA (Utilità interne) ---
    // Mappa da SudokuPuzzle a HashMap<Int, SudokuTile> per la UI
    private fun mapSudokuPuzzleToBoardState(puzzle: SudokuPuzzle): HashMap<Int, SudokuTile> {
        val board = HashMap<Int, SudokuTile>()
        puzzle.currentGraph.forEach { (row, nodeList) ->
            nodeList.forEach { node ->
                board[getHash(node.x, node.y)] = SudokuTile(
                    x = node.x,
                    y = node.y,
                    value = node.color,
                    hasFocus = false,
                    readOnly = node.readOnly
                )
            }
        }
        return board
    }

    // Mappa da HashMap<Int, SudokuTile> a SudokuPuzzle per il Repository
    private fun mapBoardStateToSudokuPuzzle(
        boardState: HashMap<Int, SudokuTile>,
        difficulty: DifficultyLevel,
        elapsedTime: Long
    ): SudokuPuzzle {
        val initialGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()
        val currentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()

        // Ricostruisci il grafo iniziale (potrebbe non essere necessario se non lo modifichi mai)
        // Per semplicità, in questo esempio, assumiamo che initialGraph non cambi mai
        // e che il repository lo gestisca. In un'app reale, o lo carichi o lo persisteti.
        // Per ora, lo ricostruiamo basandoci sulle proprietà readOnly
        boardState.values.forEach { tile ->
            if (tile.readOnly) {
                initialGraph.getOrPut(tile.y) { LinkedList() }.add(
                    SudokuNode(tile.x, tile.y, tile.value, true)
                )
            }
            currentGraph.getOrPut(tile.y) { LinkedList() }.add(
                SudokuNode(tile.x, tile.y, tile.value, tile.readOnly)
            )
        }

        // Ordina i nodi all'interno di ogni riga per x per garantire la coerenza
        initialGraph.forEach { (_, list) -> list.sortBy { it.x } }
        currentGraph.forEach { (_, list) -> list.sortBy { it.x } }


        // Nota: L'ID del puzzle sarà gestito dal repository. Lo passiamo per aggiornamenti.
        return SudokuPuzzle(
            id = currentPuzzleId, // Usiamo l'ID corrente
            boundary = 9, // Assumiamo 9x9
            difficulty = difficulty,
            initialGraph = initialGraph, // Questo deve essere l'initialGraph corretto dal database
            currentGraph = currentGraph,
            elapsedTime = elapsedTime
        )
    }
}

// Factory per instanziare ActiveGameViewModel con dipendenze
class ActiveGameViewModelFactory(
    private val gameRepository: GameRepositoryInterface,
    private val userPreferencesRepository: UserPreferencesRepositoryInterface
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveGameViewModel::class.java)) {
            return ActiveGameViewModel(gameRepository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}