package com.sudokuMaster.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.UserPreferences
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class UserPreferencesRepositoryImpl(
    private val userPreferencesDataStore: DataStore<UserPreferences>
) : UserPreferencesRepositoryInterface {

    override val userPreferencesFlow: Flow<UserPreferences> = userPreferencesDataStore.data


    override suspend fun updateAppTheme(theme: AppTheme) {
        userPreferencesDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setAppTheme(theme).build()
        }
    }


    override suspend fun updateDefaultDifficulty(difficulty: DifficultyLevel) {
        userPreferencesDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setDefaultDifficulty(difficulty).build()
        }
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        userPreferencesDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setSoundEnabled(enabled).build()
        }
    }

    override suspend fun updateMusicEnabled(enabled: Boolean) { // NUOVO METODO
        userPreferencesDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setMusicEnabled(enabled).build()
        }
    }


    override suspend fun updateLastUnfinishedGameId(gameId: Long) {
        userPreferencesDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setLastUnfinishedGameId(gameId).build()
        }
    }

    override suspend fun updateLastAccessTimestamp(timestamp: Long) {
        userPreferencesDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setLastAccessTimestamp(timestamp).build()
        }
    }

    override suspend fun getUserPreferences(): Flow<UserPreferences> {
        return userPreferencesDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(UserPreferences.getDefaultInstance())
                    throw exception
                }
            }
    }

}
