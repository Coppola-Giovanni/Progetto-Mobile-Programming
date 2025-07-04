package com.sudokuMaster.domain

import com.sudokuMaster.data.model.GameSession
import kotlinx.coroutines.flow.Flow

interface GameRepositoryInterface {
    /**
     * Inserisce una nuova sessione di gioco nel database.
     * @param gameSession L'oggetto GameSession da inserire.
     * @return Result.success(Long) con l'ID della riga inserita, Result.failure(Exception) in caso di errore.
     */
    suspend fun insertGameSession(gameSession: GameSession): Result<Long>

    /**
     * Aggiorna una sessione di gioco esistente nel database.
     * @param gameSession L'oggetto GameSession da aggiornare (deve avere un ID valido).
     * @return Result.success(Unit) in caso di successo, Result.failure(Exception) in caso di errore.
     */
    suspend fun updateGameSession(gameSession: GameSession): Result<Unit>

    /**
     * Elimina una sessione di gioco dal database.
     * @param gameSession L'oggetto GameSession da eliminare.
     * @return Result.success(Unit) in caso di successo, Result.failure(Exception) in caso di errore.
     */
    suspend fun deleteGameSession(gameSession: GameSession): Result<Unit>

    /**
     * Ottiene una sessione di gioco specifica tramite il suo ID.
     * @param sessionId L'ID della sessione di gioco.
     * @return Flow che emette la GameSession corrispondente (o null se non trovata).
     */
    fun getGameSessionById(sessionId: Long): Flow<GameSession?>

    /**
     * Ottiene tutte le sessioni di gioco.
     * @return Flow che emette una lista di tutte le GameSession.
     */
    fun getAllGameSessions(): Flow<List<GameSession>>

    /**
     * Ottiene l'ultima sessione di gioco non completata.
     * Utile per la funzione "riprendi partita".
     * @return Flow che emette l'ultima GameSession non completata (o null se nessuna).
     */
    fun getLatestUnfinishedGameSession(): Flow<GameSession?>

    // --- Operazioni e accesso alle statistiche aggregate ---

    /**
     * Fornisce un Flow reattivo delle statistiche utente aggregate.
     * Le statistiche verranno ricalcolate e riemesse ogni volta che le sessioni di gioco cambiano.
     * @return Flow che emette un oggetto UserStatistics.
     */
    fun getUserStatistics(): Flow<UserStatistics>
}