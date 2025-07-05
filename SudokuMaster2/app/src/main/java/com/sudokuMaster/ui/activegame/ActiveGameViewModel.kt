package com.sudokuMaster.ui.activegame

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
    private val userPreferencesRepository: UserPreferencesRepositoryInterface,
    private val initialGameType: String // AGGIUNTA: Parametro per il tipo di gioco
) : ViewModel() {

    // --- StateFlow per l'UI ---
    private val _uiState = MutableStateFlow(ActiveGameScreenState.LOADING)
    val uiState: StateFlow<ActiveGameScreenState> = _uiState.asStateFlow()

    private val _sudokuGrid = MutableStateFlow<List<SudokuTile>>(emptyList())
    val sudokuGrid: StateFlow<List<SudokuTile>> = _sudokuGrid.asStateFlow()

    private val _selectedTile = MutableStateFlow<SudokuTile?>(null)
    val selectedTile: StateFlow<SudokuTile?> = _selectedTile.asStateFlow()

    private val _timerState = MutableStateFlow(0L)
    val timerState: StateFlow<Long> = _timerState.asStateFlow()

    private val _difficulty = MutableStateFlow(DifficultyLevel.EASY)
    val difficulty: StateFlow<DifficultyLevel> = _difficulty.asStateFlow()

    private val _showLoading = MutableStateFlow(true)
    val showLoading: StateFlow<Boolean> = _showLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isNewRecord = MutableStateFlow(false)
    val isNewRecord: StateFlow<Boolean> = _isNewRecord.asStateFlow()

    // --- Variabili interne per la gestione del gioco ---
    private var timerJob: Job? = null
    private var currentPuzzleId: Long = 0L // ID della sessione di gioco corrente
    private lateinit var currentSudokuPuzzle: SudokuPuzzle // Il puzzle di dominio

    init {
        // Inizializza il gioco in base al tipo specificato dalla navigazione
        viewModelScope.launch {
            _showLoading.value = true
            when (initialGameType) {
                "new" -> startNewGame()
                "continue" -> loadGame()
                else -> {
                    _error.value = "Tipo di gioco non valido."
                    _showLoading.value = false
                    // Potresti voler navigare indietro o mostrare un errore più persistente
                }
            }
        }
    }

    // --- Gestione degli eventi dell'UI ---
    fun onEvent(event: ActiveGameEvent) {
        when (event) {
            is ActiveGameEvent.onInput -> onInput(event.input)
            is ActiveGameEvent.onTileFocused -> onTileFocused(event.x, event.y)
            ActiveGameEvent.OnNewGameClicked -> viewModelScope.launch { startNewGame() }
            ActiveGameEvent.OnStart -> startTimer() // Chiamato quando la schermata è attiva
            ActiveGameEvent.OnStop -> stopTimer()   // Chiamato quando la schermata esce
        }
    }

    private suspend fun startNewGame() {
        _showLoading.value = true
        _uiState.value = ActiveGameScreenState.LOADING
        _error.value = null // Resetta eventuali errori precedenti

        val userPreferences = userPreferencesRepository.getUserPreferencesFlow().first()
        val defaultDifficulty = userPreferences.defaultDifficulty

        gameRepository.createNewGameAndSave(
            difficulty = defaultDifficulty,
            onSuccess = { gameSession ->
                currentPuzzleId = gameSession.id
                currentSudokuPuzzle = gameSession.toSudokuPuzzle() // Converte la GameSession in SudokuPuzzle
                _difficulty.value = currentSudokuPuzzle.difficulty
                initializeGrid(currentSudokuPuzzle)
                _timerState.value = currentSudokuPuzzle.elapsedTime
                _showLoading.value = false
                _uiState.value = ActiveGameScreenState.ACTIVE
                startTimer()
            },
            onError = { e ->
                _error.value = "Errore durante la creazione del gioco: ${e.localizedMessage}"
                _showLoading.value = false
                _uiState.value = ActiveGameScreenState.ACTIVE // Torna a stato attivo ma con errore
                // Potresti anche voler chiamare un callback per navigare indietro, come onGameLoadError
            }
        )
    }

    private suspend fun loadGame() {
        _showLoading.value = true
        _uiState.value = ActiveGameScreenState.LOADING
        _error.value = null // Resetta eventuali errori precedenti

        gameRepository.getLatestUnfinishedGameSession(
            onSuccess = { gameSession ->
                if (gameSession.isSolved) {
                    //
                } else {
                    currentPuzzleId = gameSession.id
                    currentSudokuPuzzle = gameSession.toSudokuPuzzle()
                    _difficulty.value = currentSudokuPuzzle.difficulty
                    initializeGrid(currentSudokuPuzzle)
                    _timerState.value = currentSudokuPuzzle.elapsedTime
                    _showLoading.value = false
                    _uiState.value = ActiveGameScreenState.ACTIVE
                    startTimer()
                }
            },
            onError = { e ->
                _error.value = "Nessuna partita da continuare o errore di caricamento: ${e.localizedMessage}. Avvio una nuova partita."
            }
        )
    }

    private fun initializeGrid(puzzle: SudokuPuzzle) {
        val newGrid = mutableListOf<SudokuTile>()
        puzzle.currentGraph.forEach { (_, nodes) ->
            nodes.forEach { node ->
                newGrid.add(
                    SudokuTile(
                        x = node.x,
                        y = node.y,
                        value = node.color,
                        hasFocus = false,
                        readOnly = node.readOnly
                    )
                )
            }
        }
        _sudokuGrid.value = newGrid.sortedWith(compareBy({ it.y }, { it.x }))
    }

    private fun onInput(input: Int) {
        _selectedTile.value?.let { tile ->
            if (!tile.readOnly) {
                // Aggiorna il valore nella griglia dell'UI
                val updatedGrid = _sudokuGrid.value.toMutableList()
                val index = updatedGrid.indexOfFirst { it.x == tile.x && it.y == tile.y }
                if (index != -1) {
                    updatedGrid[index] = tile.copy(value = input)
                    _sudokuGrid.value = updatedGrid
                    _selectedTile.value = updatedGrid[index] // Aggiorna anche il selectedTile
                    saveGameProgress(updatedGrid[index])
                }
            }
        }
    }

    private fun onTileFocused(x: Int, y: Int) {
        val updatedGrid = _sudokuGrid.value.map {
            it.copy(hasFocus = (it.x == x && it.y == y))
        }
        _sudokuGrid.value = updatedGrid
        _selectedTile.value = updatedGrid.firstOrNull { it.x == x && it.y == y }
    }

    private fun startTimer() {
        timerJob?.cancel() // Cancella il timer precedente se esiste
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Aggiorna ogni secondo
                _timerState.value++
                // Ogni 10 secondi, salva il tempo
                if (_timerState.value % 10 == 0L) {
                    saveGameProgress(null, onlySaveTime = true)
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun saveGameProgress(
        updatedTile: SudokuTile? = null,
        onlySaveTime: Boolean = false
    ) = viewModelScope.launch {
        // Se non c'è un puzzle corrente, non c'è nulla da salvare
        if (!::currentSudokuPuzzle.isInitialized || currentPuzzleId == 0L) {
            return@launch
        }

        // Recupera la griglia corrente aggiornata (solo se c'è un updatedTile)
        val currentTiles = _sudokuGrid.value

        // Ricrea il SudokuPuzzle di dominio con lo stato attuale
        val updatedPuzzle = recreateSudokuPuzzle(
            tiles = currentTiles,
            difficulty = _difficulty.value,
            elapsedTime = _timerState.value
        )

        gameRepository.updateGameNodeAndSave(
            puzzle = updatedPuzzle,
            onSuccess = { gameSession ->
                currentSudokuPuzzle = gameSession.toSudokuPuzzle() // Aggiorna il puzzle di dominio con l'ultima GameSession
                if (gameSession.isSolved) {
                    stopTimer()
                    _uiState.value = ActiveGameScreenState.COMPLETE
                }
            },
            onError = { e ->
                _error.value = "Errore durante il salvataggio o la verifica del gioco: ${e.localizedMessage}"
                // Non cambiamo lo stato della UI in COMPLETE qui se c'è un errore
            }
        )
    }

    private suspend fun checkNewRecord(solveTimeSeconds: Long, difficulty: DifficultyLevel) {
        val stats = gameRepository.getUserStatistics().first()
        val currentBestTime = when (difficulty) {
            DifficultyLevel.EASY -> stats.bestSolveTimeEasyMillis
            DifficultyLevel.MEDIUM -> stats.bestSolveTimeMediumMillis
            DifficultyLevel.HARD -> stats.bestSolveTimeHardMillis
            DifficultyLevel.DIFFICULTY_UNSPECIFIED -> null
            DifficultyLevel.UNRECOGNIZED -> TODO()
        }

        if (currentBestTime == null || (solveTimeSeconds * 1000 < currentBestTime)) {
            _isNewRecord.value = true
        }
    }


    override fun onCleared() {
        super.onCleared()
        stopTimer()
        // Salva lo stato finale del gioco quando il ViewModel viene distrutto
        // (es. l'utente esce dalla schermata)
        viewModelScope.launch {
            if (::currentSudokuPuzzle.isInitialized && currentPuzzleId != 0L) {
                val updatedPuzzle = recreateSudokuPuzzle(
                    tiles = _sudokuGrid.value,
                    difficulty = _difficulty.value,
                    elapsedTime = _timerState.value
                )
                gameRepository.updateGameSession(
                    gameSession = updatedPuzzle.toGameSession(
                        existingId = currentPuzzleId,
                        isSolved = false, // Assumiamo non risolta alla chiusura forzata
                        score = 0 // Il punteggio sarà calcolato solo alla risoluzione
                    ),
                    onSuccess = { /* Log success, no UI update needed */ },
                    onError = { e ->
                        // Log errore, non fare Toast qui perché il ViewModel sta per essere distrutto
                        println("Errore nel salvataggio alla chiusura: ${e.localizedMessage}")
                    }
                )
            }
        }
    }

    // Helper per ricreare SudokuPuzzle dallo stato corrente delle tiles
    private fun recreateSudokuPuzzle(
        tiles: List<SudokuTile>,
        difficulty: DifficultyLevel,
        elapsedTime: Long
    ): SudokuPuzzle {
        val initialGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()
        val currentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()

        // Riempie initialGraph e currentGraph basandosi sulle tiles fornite
        // Devi avere l'initialGraph originale da qualche parte (es. nel currentSudokuPuzzle)
        // Oppure, se stai solo aggiornando, assicurati di usare i valori originali per readOnly
        val originalInitialGraph = currentSudokuPuzzle.initialGraph

        tiles.forEach { tile ->
            val initialValue = originalInitialGraph[tile.y]?.firstOrNull { it.x == tile.x }?.color ?: 0
            initialGraph.getOrPut(tile.y) { LinkedList() }.add(
                SudokuNode(tile.x, tile.y, initialValue, initialValue != 0)
            )
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
    private val userPreferencesRepository: UserPreferencesRepositoryInterface,
    private val initialGameType: String // AGGIUNTA: Parametro per il tipo di gioco
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActiveGameViewModel::class.java)) {
            // Passa initialGameType al ViewModel
            return ActiveGameViewModel(gameRepository, userPreferencesRepository, initialGameType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}