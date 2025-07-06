package com.sudokuMaster.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.UserPreferences
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

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



    /*override suspend fun updateShowTutorial(show: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            userPreferencesDataStore.updateData { preferences ->
                preferences.toBuilder().setShowTutorial(show).build()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }*/

    override suspend fun updateLastUnfinishedGameId(id: Long) {
        userPreferencesDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setLastUnfinishedGameId(id).build()
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
                // DataStore throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    emit(UserPreferences.getDefaultInstance()) // Emetti un'istanza di default in caso di errore di lettura
                } else {
                    throw exception // Rilancia altre eccezioni
                }
            }
    }

}
