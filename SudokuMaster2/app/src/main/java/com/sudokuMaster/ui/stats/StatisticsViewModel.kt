package com.sudokuMaster.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuMaster.domain.GameRepositoryInterface
import com.sudokuMaster.domain.UserStatistics
import com.sudokuMaster.common.DispatcherProvider // Assicurati che questo import sia corretto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val gameRepository: GameRepositoryInterface,
    private val dispatcherProvider: DispatcherProvider // Inietta DispatcherProvider
) : ViewModel() {

    private val _userStatistics = MutableStateFlow<UserStatistics?>(null)
    val userStatistics: StateFlow<UserStatistics?> = _userStatistics.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadUserStatistics()
    }

    private fun loadUserStatistics() {
        viewModelScope.launch(dispatcherProvider.provideIOContext()) {
            _isLoading.value = true
            _error.value = null
            gameRepository.getUserStatistics()
                .catch { e ->
                    _error.value = "Errore durante il caricamento delle statistiche: ${e.message}"
                    _isLoading.value = false
                }
                .collect { stats ->
                    _userStatistics.value = stats
                    _isLoading.value = false
                }
        }
    }

    // Factory per instanziare StatisticsViewModel con dipendenze
    class Factory(
        private val gameRepository: GameRepositoryInterface,
        private val dispatcherProvider: DispatcherProvider
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                return StatisticsViewModel(gameRepository, dispatcherProvider) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}