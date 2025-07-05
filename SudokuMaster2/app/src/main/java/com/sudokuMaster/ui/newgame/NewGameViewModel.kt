package com.sudokuMaster.ui.newgame

import androidx.datastore.core.IOException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.UserPreferences
import com.sudokuMaster.domain.GameRepositoryInterface
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import com.sudokuMaster.domain.UserStatistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NewGameScreenState {
    object LOADING : NewGameScreenState()
    data class LOADED(
        val settings: UserPreferences, // O un modello UI più leggero se necessario
        val userStatistics: UserStatistics
    ) : NewGameScreenState()
    object ERROR : NewGameScreenState()
    object NAVIGATE_TO_GAME : NewGameScreenState() // Per segnalare la navigazione
}

class NewGameViewModel(
    private val gameRepository: GameRepositoryInterface,
    private val userPreferencesRepository: UserPreferencesRepositoryInterface // Per settings e per recuperare l'ID dell'ultimo gioco
    // NON ti serve StatisticsRepositoryInterface separata se getUserStatistics è in GameRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow<NewGameScreenState>(NewGameScreenState.LOADING)
    val screenState: StateFlow<NewGameScreenState> = _screenState.asStateFlow()

    private val _currentSettings = MutableStateFlow<UserPreferences?>(null)
    val currentSettings: StateFlow<UserPreferences?> = _currentSettings.asStateFlow()

    private val _userStatistics = MutableStateFlow<UserStatistics?>(null)
    val userStatistics: StateFlow<UserStatistics?> = _userStatistics.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _screenState.value = NewGameScreenState.LOADING
            try {
                // Ottieni le preferenze utente (che includono le impostazioni)
                userPreferencesRepository.getUserPreferencesFlow().collect { prefs ->
                    _currentSettings.value = prefs
                    // Ottieni le statistiche utente
                    gameRepository.getUserStatistics().collect { stats ->
                        _userStatistics.value = stats
                        _screenState.value = NewGameScreenState.LOADED(prefs, stats)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _screenState.value = NewGameScreenState.ERROR
            }
        }
    }


    fun onDifficultyChanged(newDifficulty: DifficultyLevel) {
        viewModelScope.launch {
            _currentSettings.value?.let { current ->
                // La tua UserPreferencesRepositoryImpl restituisce Result<Unit>
                val result = userPreferencesRepository.updateDefaultDifficulty(newDifficulty)
                result.onFailure {
                    // Gestisci l'errore se l'aggiornamento delle preferenze fallisce
                    _screenState.value = NewGameScreenState.ERROR // O un errore più specifico
                }
                // Se successo, il Flow in loadData() aggiornerà _currentSettings automaticamente
            }
        }
    }

    fun onSizeChanged(newBoundary: Int) {
        // La tua UserPreferences non ha un campo per la dimensione del bordo.
        // Dovrai aggiungerlo a UserPreferences.proto e UserPreferencesDataStore.
        // Per ora, assumo che tu voglia aggiornare una "settings" generica.
        // Se il tuo gioco supporta solo 9x9 e 4x4, e l'API fornisce solo 9x9,
        // potresti dover riconsiderare come gestisci la "boundary"
        // in relazione alle impostazioni utente e alla creazione del puzzle.
        // Se la dimensione del bordo influisce sulla difficoltà dell'API, devi pensare a come mapparla.
    }

    fun onDonePressed() {
        viewModelScope.launch {
            _screenState.value = NewGameScreenState.LOADING
            try {
                val currentDifficulty = _currentSettings.value?.defaultDifficulty ?: DifficultyLevel.EASY
                val newGameSession = gameRepository.createNewGameAndSave(currentDifficulty)
                _screenState.value = NewGameScreenState.NAVIGATE_TO_GAME
            } catch (e: IOException) {
                e.printStackTrace()
                _screenState.value = NewGameScreenState.ERROR // Errore di rete/I/O
            } catch (e: Exception) {
                e.printStackTrace()
                _screenState.value = NewGameScreenState.ERROR // Altro errore
            }
        }
    }
}
