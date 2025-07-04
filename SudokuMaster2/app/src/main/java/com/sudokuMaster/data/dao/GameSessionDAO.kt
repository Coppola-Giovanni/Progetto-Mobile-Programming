package com.sudokuMaster.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sudokuMaster.data.model.GameSession
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    @Insert
    suspend fun insertGameSession(gameSession: GameSession): Long

    @Update
    suspend fun updateGameSession(gameSession: GameSession)

    @Delete
    suspend fun deleteGameSession(gameSession: GameSession)

    @Query("SELECT * FROM game_sessions WHERE id = :sessionId")
    fun getGameSessionById(sessionId: Long): Flow<GameSession?>

    /**
     * Ottiene tutte le sessioni di gioco, ordinate dalla più recente alla meno recente.
     * Restituisce un Flow che emette una lista di tutte le GameSession
     * ogni volta che la tabella 'game_sessions' cambia.
     * @return Un Flow di List<GameSession>.
     */
    @Query("SELECT * FROM game_sessions ORDER BY date_played_millis DESC")
    fun getAllGameSessions(): Flow<List<GameSession>>

    /**
     * Ottiene l'ultima sessione di gioco non ancora completata.
     * Utile per la funzione "riprendi partita".
     * Restituisce un Flow che emette l'ultima GameSession non completata (o null se nessuna)
     * ogni volta che lo stato della partita cambia.
     * @return Un Flow di GameSession?.
     */
    @Query("SELECT * FROM game_sessions WHERE is_solved = 0 ORDER BY start_time_millis DESC LIMIT 1")
    fun getLatestUnfinishedGameSession(): Flow<GameSession?>

    /**
     * Ottiene tutte le sessioni di gioco risolte, ordinate dal punteggio più alto.
     * @return Un Flow di List<GameSession>.
     */
    @Query("SELECT * FROM game_sessions WHERE is_solved = 1 ORDER BY points_scored DESC")
    fun getSolvedGameSessionsSortedByScore(): Flow<List<GameSession>>
}
