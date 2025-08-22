package com.sudokuMaster.domain

import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.model.GameSession
import com.sudokuMaster.data.model.UserStatistics
import kotlinx.coroutines.flow.Flow

interface GameRepositoryInterface {

    suspend fun createNewGameAndSave(
        difficulty: DifficultyLevel,
        onSuccess: (GameSession) -> Unit,
        onError: (Throwable) -> Unit
    )

    suspend fun getLatestUnfinishedGameSession(
        onSuccess: (GameSession) -> Unit,
        onError: (Throwable) -> Unit
    )

    suspend fun updateGameSession(
        gameSession: GameSession,
        onSuccess: (GameSession) -> Unit,
        onError: (Throwable) -> Unit
    )

    suspend fun getUserStatistics(): Flow<UserStatistics?>
}
