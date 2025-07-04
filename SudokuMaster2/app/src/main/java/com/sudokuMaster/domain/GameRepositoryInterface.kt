package com.sudokuMaster.domain

import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.model.GameSession
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

    // Questo metodo ora riceve l'intero SudokuPuzzle aggiornato
    // e restituisce la GameSession aggiornata con lo stato di risoluzione.
    suspend fun updateGameNodeAndSave(
        puzzle: SudokuPuzzle,
        onSuccess: (GameSession) -> Unit, // Restituisce la GameSession aggiornata
        onError: (Throwable) -> Unit
    )

    // Questo è un metodo più generico per aggiornare una GameSession esistente.
    // Utile per il salvataggio dei progressi quando si esce, o altre logiche.
    suspend fun updateGameSession(
        gameSession: GameSession,
        onSuccess: (GameSession) -> Unit,
        onError: (Throwable) -> Unit
    )

    // Ora restituisce un Flow di UserStatistics, come da implementazione
    suspend fun getUserStatistics(): Flow<UserStatistics>
}
