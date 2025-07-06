package com.sudokuMaster.domain

import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepositoryInterface {
    val userPreferencesFlow: Flow<UserPreferences>

    suspend fun updateAppTheme(theme: AppTheme)
    suspend fun updateDefaultDifficulty(difficulty: DifficultyLevel)
    suspend fun updateSoundEnabled(enabled: Boolean)
    //suspend fun updateShowTutorial(show: Boolean) not implemented yet!!
    suspend fun updateLastUnfinishedGameId(gameId: Long)
    suspend fun updateLastAccessTimestamp(timestamp: Long)

    suspend fun getUserPreferences(): Flow<UserPreferences>

}