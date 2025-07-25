package com.sudokuMaster.ui.userpreferences

import androidx.annotation.RawRes
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
    private val userPreferencesRepository: UserPreferencesRepositoryInterface,
    private val soundAndMusicPlayer: SoundAndMusicPlayer
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

    fun updateMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.updateMusicEnabled(enabled)
        }
    }

    fun playSoundEffect(@RawRes soundResId: Int) {
        soundAndMusicPlayer.playSoundEffect(soundResId)
    }

    fun pauseBackgroundMusic() {
        soundAndMusicPlayer.pauseBackgroundMusic()
    }

    fun resumeBackgroundMusic() {
        viewModelScope.launch {
            val prefs = userPreferencesFlow.value
            if (prefs?.musicEnabled == true) {
                soundAndMusicPlayer.resumeBackgroundMusic()
            }
        }
    }


    class UserPreferencesViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepositoryInterface,
        private val soundAndMusicPlayer: SoundAndMusicPlayer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserPreferencesViewModel::class.java)) {
                // Passa soundAndMusicPlayer al costruttore del ViewModel
                return UserPreferencesViewModel(userPreferencesRepository, soundAndMusicPlayer) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

