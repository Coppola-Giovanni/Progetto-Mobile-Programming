package com.sudokuMaster.ui.userpreferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserPreferencesViewModel(
    private val userPreferencesRepository: UserPreferencesRepositoryInterface
) : ViewModel() {

    val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesRepository.updateAppTheme(theme)
        }
    }

    fun updateDefaultDifficulty(difficulty: DifficultyLevel) {
        viewModelScope.launch {
            userPreferencesRepository.updateDefaultDifficulty(difficulty)
        }
    }

    fun updateSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateSoundEnabled(enabled)
        }
    }

    // ViewModel Factory per l'iniezione delle dipendenze
    class UserPreferencesViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepositoryInterface
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserPreferencesViewModel::class.java)) {
                return UserPreferencesViewModel(userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

