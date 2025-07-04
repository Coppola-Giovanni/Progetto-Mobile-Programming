package com.sudokuMaster.domain

import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepositoryInterface {
    fun getUserPreferencesFlow(): Flow<UserPreferences>

    suspend fun updateAppTheme(theme: AppTheme): Result<Unit>
    suspend fun updateDefaultDifficulty(difficulty: DifficultyLevel): Result<Unit>
    suspend fun updateSoundEnabled(enabled: Boolean): Result<Unit>
    //suspend fun updateShowTutorial(show: Boolean): Result<Unit> not implemented yet!!
    suspend fun updateLastUnfinishedGameId(gameId: Long): Result<Unit>

    suspend fun getUserPreferences(): Result<UserPreferences>

}