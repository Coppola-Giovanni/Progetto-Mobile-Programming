package com.sudokuMaster.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.dao.GameSessionDao
import com.sudokuMaster.data.model.GameSession
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.domain.GameRepositoryInterface
import com.sudokuMaster.domain.Settings
import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import com.sudokuMaster.domain.UserStatistics
import com.sudokuMaster.domain.getHash
import com.sudokuMaster.logic.buildNewSudokuPuzzleFromGrid
import com.sudokuMaster.logic.puzzleIsComplete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.LinkedList

class GameRepositoryImpl(
    private val gameSessionDao: GameSessionDao,
    private val userPreferencesRepository: UserPreferencesRepositoryInterface, // Per accedere a DataStore
    private val sudokuRemoteDataSource: SudokuRemoteDataSource
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

    override suspend fun getGameSessionById(sessionId: Long): Flow<GameSession?> {
        return gameSessionDao.getGameSessionById(sessionId)
    }

    override suspend fun getAllGameSessions(): Flow<List<GameSession>> {
        return gameSessionDao.getAllGameSessions()
    }

    override suspend fun getLatestUnfinishedGameSession(): Flow<GameSession?> {
        return gameSessionDao.getLatestUnfinishedGameSession()
    }

    // --- Logica di alto livello per la gestione delle partite ---

    /**
     * Crea una nuova partita di Sudoku con le impostazioni fornite, la salva nel database Room,
     * e aggiorna l'ID dell'ultima partita non completata in DataStore.
     * @param settings Le impostazioni di dominio per la nuova partita (contengono DifficultyLevel).
     * @return Result.success(Long) con l'ID della nuova GameSession creata, Result.failure(Exception) in caso di errore.
     */
    override suspend fun createNewGameAndSave(settings: Settings): Result<Long> = withContext(Dispatchers.IO) {
        // La funzione deve restituire un Result<Long>.
        // Inizializziamo un Result<Long> che aggiorneremo
        // o restituiremo in caso di successo/fallimento della chiamata remota.
        val remoteDataResult = sudokuRemoteDataSource.getNewSudokuPuzzleData(settings.difficulty)

        // Usiamo `fold` per gestire sia il successo che il fallimento in un'unica espressione.
        return@withContext remoteDataResult.fold(
            onSuccess = { (initialGridData, actualDifficulty) ->
                try {
                    val newSudokuPuzzle = buildNewSudokuPuzzleFromGrid(initialGridData, actualDifficulty)
                    val newGameSession = newSudokuPuzzle.toGameSessionEntity()
                    val gameId = gameSessionDao.insertGameSession(newGameSession)

                    userPreferencesRepository.updateLastUnfinishedGameId(gameId)
                    Result.success(gameId) // Restituisce Result.success<Long>
                } catch (e: Exception) {
                    // Se c'è un errore durante il salvataggio o la costruzione del puzzle
                    Result.failure(Exception("Failed to save new Sudoku game: ${e.localizedMessage}", e))
                }
            },
            onFailure = { e ->
                // Se la chiamata API fallisce
                Result.failure(Exception("Failed to get Sudoku from API: ${e.localizedMessage}", e)) // Restituisce Result.failure<Long>
            }
        )
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
    override suspend fun updateGameNodeAndSave(gameId: Long, x: Int, y: Int, value: Int, newElapsedTime: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val gameSession = gameSessionDao.getGameSessionById(gameId).first()
                ?: throw NoSuchElementException("Game session with ID $gameId not found.")

            val type = object : TypeToken<LinkedHashMap<Int, LinkedList<SudokuNode>>>() {}.type
            val currentGraph: LinkedHashMap<Int, LinkedList<SudokuNode>> = gson.fromJson(gameSession.currentGrid, type)

            // Aggiorna il nodo specifico
            currentGraph[y]?.find { it.x == x }?.color = value

            // Mappa di nuovo a GameSession (con l'initialGrid originale)
            val updatedGameSession = gameSession.copy(
                currentGrid = gson.toJson(currentGraph),
                durationSeconds = newElapsedTime,
                isSolved = false // Ricalcolare se risolto, o impostare a falso se si cambia una cella
            )
            gameSessionDao.updateGameSession(updatedGameSession)

            // Qui dovresti ricalcolare se il puzzle è completo dopo l'aggiornamento
            // richiamando la logica di validazione del puzzle.
            // Per ora, restituisco sempre false per `isCompleted`.
            Result.success(false)
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
    private fun SudokuPuzzle.toGameSessionEntity(): GameSession {
        // Questa è la mappatura dal SudokuPuzzle di dominio all'entità Room GameSession
        val initialGridJson = gson.toJson(this.initialGraph)
        val currentGridJson = gson.toJson(this.currentGraph)

        return GameSession(
            id = this.id, // Se l'ID è 0, Room lo genererà automaticamente (insert)
            difficulty = this.difficulty.name, // Salva l'enum DifficultyLevel come stringa
            currentGrid = currentGridJson,
            initialGrid = initialGridJson,
            startTimeMillis = System.currentTimeMillis(),
            endTimeMillis = null,
            durationSeconds = this.elapsedTime,
            score = 0,
            isSolved = false,
            datePlayedMillis = System.currentTimeMillis()
        )
    }

    /**
     * Converte una GameSession entità Room in un SudokuPuzzle di dominio.
     */
    private fun GameSession.toSudokuPuzzleDomain(): SudokuPuzzle {
        val graphType = object : TypeToken<LinkedHashMap<Int, LinkedList<SudokuNode>>>() {}.type

        // Deserializza sia initialGrid che currentGrid
        val deserializedInitialGraph: LinkedHashMap<Int, LinkedList<SudokuNode>> = gson.fromJson(this.initialGrid, graphType)
        val deserializedCurrentGraph: LinkedHashMap<Int, LinkedList<SudokuNode>> = gson.fromJson(this.currentGrid, graphType)

        val derivedBoundary = 9 // Assumi 9x9
        val protoDifficulty = DifficultyLevel.valueOf(this.difficulty)

        return SudokuPuzzle(
            id = this.id,
            boundary = derivedBoundary,
            difficulty = protoDifficulty,
            initialGraph = deserializedInitialGraph, // Assegna la griglia iniziale
            currentGraph = deserializedCurrentGraph, // Assegna la griglia corrente
            elapsedTime = this.durationSeconds ?: 0L
        )
    }

}




