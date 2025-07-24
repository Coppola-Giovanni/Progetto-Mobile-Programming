package com.sudokuMaster.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    userPreferencesRepository: UserPreferencesRepositoryInterface
) : ViewModel() {
    val userPreferencesFlow = userPreferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    class HomeViewModelFactory(
        private val userPreferencesRepository: UserPreferencesRepositoryInterface
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
