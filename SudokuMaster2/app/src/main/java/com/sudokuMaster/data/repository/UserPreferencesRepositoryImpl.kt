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

    override fun getUserPreferencesFlow(): Flow<UserPreferences> {
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

    override suspend fun updateAppTheme(theme: AppTheme): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            userPreferencesDataStore.updateData { preferences ->
                preferences.toBuilder().setAppTheme(theme).build()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDefaultDifficulty(difficulty: DifficultyLevel): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            userPreferencesDataStore.updateData { preferences ->
                preferences.toBuilder().setDefaultDifficulty(difficulty).build()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSoundEnabled(enabled: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            userPreferencesDataStore.updateData { preferences ->
                preferences.toBuilder().setSoundEnabled(enabled).build()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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

    override suspend fun updateLastUnfinishedGameId(gameId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            userPreferencesDataStore.updateData { preferences ->
                preferences.toBuilder().setLastUnfinishedGameId(gameId).build()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserPreferences(): Flow<UserPreferences> {
        return userPreferencesDataStore.data
            .map { preferences ->
                // Qui potresti mappare le 'preferences' (dal DataStore) al tuo dominio UserPreferences
                // Se il tuo DataStore è già DataStore<UserPreferences>, non serve una mappatura complessa.
                // Assumiamo che UserPreferences sia la stessa classe che viene serializzata/deserializzata dal DataStore.
                preferences // Restituisci direttamente l'oggetto UserPreferences
            }
    }
}
