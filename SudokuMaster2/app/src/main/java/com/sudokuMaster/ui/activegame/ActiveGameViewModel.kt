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
import java.util.LinkedList
import java.util.concurrent.TimeUnit

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
    COMPLETE
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
            is ActiveGameEvent.onInput -> onInput(event.input)
            is ActiveGameEvent.onTileFocused -> onTileFocused(event.x, event.y)
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

            gameRepository.updateGameNodeAndSave(
                puzzle, // Passa l'intero puzzle aggiornato
                onSuccess = { updatedGameSession ->
                    // L'updateGameNodeAndSave ora restituisce la GameSession aggiornata
                    // Possiamo estrarre isSolved da lì.
                    if (updatedGameSession.isSolved) {
                        timerJob?.cancel()
                        val solveTimeMillis = updatedGameSession.durationSeconds?.let { it * 1000L }
                            ?: (updatedGameSession.endTimeMillis?.minus(updatedGameSession.startTimeMillis)
                                ?: 0L) // Fallback se duration_seconds è nullo
                        checkIfNewRecord(solveTimeMillis)
                    }
                },
                onError = {
                    // Mostra errore (da gestire tramite ActiveGameContainer)
                    // Per ora, non abbiamo un accesso diretto al container qui.
                    // Questo sarà gestito dalla UI che osserva lo stato.
                }
            )
        }
    }

    private fun onNewGameClicked() = viewModelScope.launch {
        // Salva lo stato corrente se non è completo, poi naviga
        if (_screenState.value != ActiveGameScreenState.COMPLETE) {
            val currentBoard = _boardState.value
            val currentDifficulty = _difficulty.value
            val currentElapsedTime = _timerState.value

            val puzzleToSave = mapBoardStateToSudokuPuzzle(currentBoard, currentDifficulty, currentElapsedTime)

            gameRepository.updateGameSession(
                gameSession = puzzleToSave.toGameSession(currentPuzzleId), // Converti in GameSession
                onSuccess = { navigateToNewGame() },
                onError = { navigateToNewGame() /* Errore nel salvataggio, naviga comunque */ }
            )
        } else {
            navigateToNewGame()
        }
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
                val currentBoard = _boardState.value
                val currentDifficulty = _difficulty.value
                val currentElapsedTime = _timerState.value

                val puzzleToSave = mapBoardStateToSudokuPuzzle(currentBoard, currentDifficulty, currentElapsedTime)

                gameRepository.updateGameSession(
                    gameSession = puzzleToSave.toGameSession(currentPuzzleId), // Converti in GameSession
                    onSuccess = { timerJob?.cancel() }, // Annulla il timer
                    onError = {
                        // Mostra errore (da gestire dalla UI)
                        timerJob?.cancel()
                    }
                )
            }
        } else {
            timerJob?.cancel() // Se il gioco è completo, annulla solo il timer
        }
    }

    private fun loadGame() = viewModelScope.launch {
        _screenState.value = ActiveGameScreenState.LOADING
        gameRepository.getLatestUnfinishedGameSession(
            onSuccess = { gameSession ->
                // Aggiorna currentPuzzleId
                currentPuzzleId = gameSession.id

                val puzzle = gameSession.toSudokuPuzzle()
                _difficulty.value = puzzle.difficulty
                _timerState.value = puzzle.elapsedTime
                _boardState.value = mapSudokuPuzzleToBoardState(puzzle)

                // Se il puzzle è già risolto, mostra lo stato completo
                if (gameSession.isSolved) {
                    _screenState.value = ActiveGameScreenState.COMPLETE
                    _isNewRecord.value = false // Non è un nuovo record se era già completo
                } else {
                    _screenState.value = ActiveGameScreenState.ACTIVE
                    startTimer()
                }
            },
            onError = {
                // Nessun gioco non finito, avvia una nuova partita con difficoltà predefinita
                viewModelScope.launch {
                    val settings = userPreferencesRepository.getUserPreferences().first()
                    val defaultDifficulty = settings.defaultDifficulty

                    gameRepository.createNewGameAndSave(
                        difficulty = defaultDifficulty,
                        onSuccess = { newGameSession ->
                            currentPuzzleId = newGameSession.id
                            val newPuzzle = newGameSession.toSudokuPuzzle()
                            _difficulty.value = newPuzzle.difficulty
                            _timerState.value = newPuzzle.elapsedTime
                            _boardState.value = mapSudokuPuzzleToBoardState(newPuzzle)
                            _screenState.value = ActiveGameScreenState.ACTIVE
                            startTimer()
                        },
                        onError = {
                            // Errore nella creazione di un nuovo gioco, mostra errore
                            _screenState.value = ActiveGameScreenState.COMPLETE // Forse un fallback più adatto
                            // E notificare ActiveGameActivity per mostrare un errore generico
                        }
                    )
                }
            }
        )
    }

    private fun startTimer() {
        timerJob?.cancel() // Annulla qualsiasi timer precedente
        timerJob = viewModelScope.launch {
            while (_screenState.value == ActiveGameScreenState.ACTIVE) {
                delay(1000) // Aspetta 1 secondo
                _timerState.value = _timerState.value + 1
            }
        }
    }

    private fun checkIfNewRecord(finalTimeMillis: Long) = viewModelScope.launch {
        val userStats = gameRepository.getUserStatistics().first()
        val currentDifficulty = _difficulty.value

        val isRecord = when (currentDifficulty) {
            DifficultyLevel.EASY -> finalTimeMillis < userStats.bestSolveTimeEasyMillis!!
            DifficultyLevel.MEDIUM -> finalTimeMillis < userStats.bestSolveTimeMediumMillis!!
            DifficultyLevel.HARD -> finalTimeMillis < userStats.bestSolveTimeHardMillis!!
            else -> false
        }

        _isNewRecord.value = isRecord
        _screenState.value = ActiveGameScreenState.COMPLETE
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