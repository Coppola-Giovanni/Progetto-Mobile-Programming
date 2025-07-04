package com.sudokuMaster.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.dao.GameSessionDao
import com.sudokuMaster.data.model.GameSession
import com.sudokuMaster.domain.GameRepositoryInterface
import com.sudokuMaster.domain.Settings
import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import com.sudokuMaster.domain.UserStatistics
import com.sudokuMaster.domain.getHash
import com.sudokuMaster.logic.puzzleIsComplete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.LinkedList

class GameRepositoryImpl(
    private val gameSessionDao: GameSessionDao,
    private val userPreferencesRepository: UserPreferencesRepositoryInterface // Per accedere a DataStore
) : GameRepositoryInterface {

    private val gson = Gson()


    // --- Implementazione delle funzioni sulle singole partite / sessioni di gioco ---

    override suspend fun insertGameSession(gameSession: GameSession): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val newId = gameSessionDao.insertGameSession(gameSession)
            Result.success(newId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGameSession(gameSession: GameSession): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            gameSessionDao.updateGameSession(gameSession)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteGameSession(gameSession: GameSession): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            gameSessionDao.deleteGameSession(gameSession)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getGameSessionById(sessionId: Long): Flow<GameSession?> {
        return gameSessionDao.getGameSessionById(sessionId)
    }

    override fun getAllGameSessions(): Flow<List<GameSession>> {
        return gameSessionDao.getAllGameSessions()
    }

    override fun getLatestUnfinishedGameSession(): Flow<GameSession?> {
        return gameSessionDao.getLatestUnfinishedGameSession()
    }

    // --- Logica di alto livello per la gestione delle partite ---

    /**
     * Crea una nuova partita di Sudoku con le impostazioni fornite, la salva nel database Room,
     * e aggiorna l'ID dell'ultima partita non completata in DataStore.
     * @param settings Le impostazioni di dominio per la nuova partita (contengono DifficultyLevel).
     * @return Result.success(Long) con l'ID della nuova GameSession creata, Result.failure(Exception) in caso di errore.
     */
    suspend fun createNewGameAndSave(settings: Settings): Result<Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val newSudokuPuzzle = SudokuPuzzle(settings.boundary, settings.difficulty)
            val newGameSession = newSudokuPuzzle.toGameSessionEntity()

            val gameId = gameSessionDao.insertGameSession(newGameSession)

            // Aggiorna l'ID dell'ultima partita non completata in DataStore
            userPreferencesRepository.updateLastUnfinishedGameId(gameId)

            Result.success(gameId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Aggiorna un nodo specifico in una partita esistente e il tempo trascorso.
     * Questa funzione recupera la partita dal DB, la modifica e la salva di nuovo.
     * Aggiorna anche il campo `isSolved` se la partita è completata.
     * @param gameId L'ID della GameSession da aggiornare.
     * @param x Coordinata X del nodo.
     * @param y Coordinata Y del nodo.
     * @param color Nuovo valore del nodo.
     * @param elapsedTime Tempo trascorso attuale della partita.
     * @return Result.success(Boolean) indicando se la partita è stata completata con questo aggiornamento.
     */
    suspend fun updateGameNodeAndSave(
        gameId: Long,
        x: Int,
        y: Int,
        color: Int,
        elapsedTime: Long
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Ottieni la GameSession corrente dal DAO
            val gameSession = gameSessionDao.getGameSessionById(gameId).first()
                ?: return@withContext Result.failure(NoSuchElementException("GameSession with ID $gameId not found."))

            // Mappa l'entità GameSession a SudokuPuzzle di dominio per la manipolazione
            val sudokuPuzzle = gameSession.toSudokuPuzzleDomain()

            // Aggiorna il nodo e il tempo
            sudokuPuzzle.graph[getHash(x, y)]?.first?.color = color
            sudokuPuzzle.elapsedTime = elapsedTime

            // Controlla se la partita è completa
            val isComplete = puzzleIsComplete(sudokuPuzzle)

            // Converte il SudokuPuzzle aggiornato di nuovo in GameSession per l'aggiornamento
            // Passiamo l'ID esistente per assicurarci che Room faccia un UPDATE e non un INSERT
            val updatedGameSession = sudokuPuzzle.toGameSessionEntity(gameId).copy(
                isSolved = isComplete,
                endTimeMillis = if (isComplete) System.currentTimeMillis() else null,
                durationSeconds = sudokuPuzzle.elapsedTime
            )

            gameSessionDao.updateGameSession(updatedGameSession)

            // Se la partita è completa, potremmo voler rimuovere il suo ID dall'ultima partita non completata in DataStore
            if (isComplete) {
                userPreferencesRepository.updateLastUnfinishedGameId(0L) // Nessuna partita non completata
            }

            Result.success(isComplete)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- Implementazione delle operazioni e accesso alle statistiche aggregate ---

    /**
     * Fornisce un Flow reattivo delle statistiche utente.
     * Ogni volta che le GameSession cambiano nel DB, le statistiche vengono ricalcolate e riemesse.
     * @return Un Flow di UserStatistics.
     */
    override fun getUserStatistics(): Flow<UserStatistics> {
        return gameSessionDao.getAllGameSessions().map { gameSessions ->
            calculateUserStatistics(gameSessions)
        }
    }

    // Logica privata per il calcolo delle statistiche da una lista di GameSession
    private fun calculateUserStatistics(gameSessions: List<GameSession>): UserStatistics {
        val solvedGames = gameSessions.filter { it.isSolved }

        val totalGamesPlayed = gameSessions.size
        val totalGamesSolved = solvedGames.size

        val totalSolveTime = solvedGames.sumOf { it.durationSeconds ?: 0L }
        val averageSolveTimeMillis = if (totalGamesSolved > 0) totalSolveTime / totalGamesSolved * 1000L else 0L

        // Assicurati che i nomi delle difficoltà usati qui corrispondano a `difficulty.name` di DifficultyLevel
        val bestSolveTimeEasy = solvedGames.filter { it.difficulty == DifficultyLevel.EASY.name }
            .minOfOrNull { it.durationSeconds ?: Long.MAX_VALUE }?.times(1000L)
        val bestSolveTimeMedium = solvedGames.filter { it.difficulty == DifficultyLevel.MEDIUM.name }
            .minOfOrNull { it.durationSeconds ?: Long.MAX_VALUE }?.times(1000L)
        val bestSolveTimeHard = solvedGames.filter { it.difficulty == DifficultyLevel.HARD.name }
            .minOfOrNull { it.durationSeconds ?: Long.MAX_VALUE }?.times(1000L)


        return UserStatistics(
            totalGamesPlayed = totalGamesPlayed,
            totalGamesSolved = totalGamesSolved,
            averageSolveTimeMillis = averageSolveTimeMillis,
            bestSolveTimeEasyMillis = bestSolveTimeEasy,
            bestSolveTimeMediumMillis = bestSolveTimeMedium,
            bestSolveTimeHardMillis = bestSolveTimeHard,
        )
    }

    // --- Funzioni di mappatura (possono essere in un file Mappers.kt separato per chiarezza) ---

    /**
     * Converte un SudokuPuzzle di dominio in una GameSession entità Room.
     * @param existingId Se fornito, indica che si sta aggiornando una GameSession esistente.
     * Altrimenti, Room genererà un nuovo ID.
     */
    private fun SudokuPuzzle.toGameSessionEntity(existingId: Long = 0L): GameSession {
        val serializedGraph = gson.toJson(this.graph)

        return GameSession(
            id = existingId.takeIf { it != 0L } ?: 0L, // Se existingId è valido, usalo, altrimenti 0 per autoGenerate
            difficulty = this.difficulty.name, // Salva il nome dell'enum DifficultyLevel
            startTimeMillis = System.currentTimeMillis(), // Questo può essere l'inizio effettivo della sessione
            endTimeMillis = if (puzzleIsComplete(this)) System.currentTimeMillis() else null,
            durationSeconds = this.elapsedTime,
            score = 0, // Implementa la logica del punteggio qui
            isSolved = puzzleIsComplete(this),
            initialGrid = serializedGraph, // Potresti voler salvare una versione "pulita" o iniziale
            currentGrid = serializedGraph, // La griglia attuale
            datePlayedMillis = System.currentTimeMillis() // Data dell'ultima modifica/creazione
        )
    }

    /**
     * Converte una GameSession entità Room in un SudokuPuzzle di dominio.
     */
    private fun GameSession.toSudokuPuzzleDomain(): SudokuPuzzle {
        val type = object : TypeToken<LinkedHashMap<Int, LinkedList<SudokuNode>>>() {}.type
        val deserializedGraph: LinkedHashMap<Int, LinkedList<SudokuNode>> = gson.fromJson(this.currentGrid, type)

        // Il boundary non è persistito in GameSession. Assumiamo un valore predefinito o lo deriviamo.
        val derivedBoundary = 9 // O un altro valore logico per la tua app
        val difficultyLevel = DifficultyLevel.valueOf(this.difficulty) // Converte il nome della stringa in DifficultyLevel

        return SudokuPuzzle(
            boundary = 9,
            difficulty = difficultyLevel,
            graph = deserializedGraph,
            elapsedTime = this.durationSeconds ?: 0L
        )
    }
}



