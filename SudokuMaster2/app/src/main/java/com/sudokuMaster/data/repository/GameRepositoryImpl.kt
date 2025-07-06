package com.sudokuMaster.data.repository

import android.util.Log
import com.sudokuMaster.common.toDifficultyLevel
import com.sudokuMaster.common.toGameSession
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.model.GameSession
import com.sudokuMaster.data.dao.GameSessionDao
import com.sudokuMaster.data.dao.UserStatisticsDAO
import com.sudokuMaster.data.model.UserStatistics
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.domain.GameRepositoryInterface
import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.LinkedList

class GameRepositoryImpl(
    private val gameSessionDao: GameSessionDao,
    private val userStatisticsDao: UserStatisticsDAO,
    private val userPreferencesRepository: UserPreferencesRepositoryInterface,
    private val sudokuRemoteDataSource: SudokuRemoteDataSource
) : GameRepositoryInterface {

    override suspend fun createNewGameAndSave(
        difficulty: DifficultyLevel,
        onSuccess: (GameSession) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            // 1. Chiedi un nuovo puzzle al remote data source
            // La difficoltà passata è quella richiesta, non è sempre HARD.
            val apiResult = sudokuRemoteDataSource.getNewSudokuPuzzleData(difficulty)

            apiResult.onSuccess { (initialGridData, actualDifficulty) ->
                // initialGridData è List<List<Int>>, actualDifficulty è DifficultyLevel
                // Non abbiamo la 'solution' dal SudokuRemoteDataSource, useremo initialGridData per costruire initialGraph e currentGraph.
                // Le celle readOnly saranno quelle che non sono 0 in initialGridData.

                val boundary = 9 // Assumiamo 9x9

                // Costruisci initialGraph e currentGraph dal initialGridData ricevuto dall'API
                val initialGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()
                val currentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()

                for (row in 0 until boundary) {
                    val initialRowList = LinkedList<SudokuNode>()
                    val currentRowList = LinkedList<SudokuNode>()
                    for (col in 0 until boundary) {
                        val value = initialGridData[row][col] // Valore iniziale dal board dell'API
                        val isReadOnly = value != 0

                        initialRowList.add(
                            SudokuNode(
                                x = col,
                                y = row,
                                color = value,
                                readOnly = isReadOnly
                            )
                        )
                        currentRowList.add(
                            SudokuNode(
                                x = col,
                                y = row,
                                color = value, // Inizialmente con i valori non vuoti, 0 altrimenti
                                readOnly = isReadOnly
                            )
                        )
                    }
                    initialGraph[row] = initialRowList
                    currentGraph[row] = currentRowList
                }
                initialGraph.forEach { (_, list) -> list.sortBy { it.x } } // Ordina per x
                currentGraph.forEach { (_, list) -> list.sortBy { it.x } } // Ordina per x


                val newSudokuPuzzle = SudokuPuzzle(
                    id = 0L, // L'ID sarà generato da Room
                    boundary = boundary,
                    difficulty = actualDifficulty, // Usa la difficoltà effettiva restituita dall'API
                    initialGraph = initialGraph,
                    currentGraph = currentGraph,
                    elapsedTime = 0L
                )

                // 3. Converti il SudokuPuzzle in GameSession per Room e salvalo
                val newGameSession = newSudokuPuzzle.toGameSession(
                    existingId = 0L, // Nuovo gioco
                    isSolved = false, // Non risolto all'inizio
                    score = 0
                ).copy(startTimeMillis = System.currentTimeMillis()) // Imposta il tempo di inizio

                val gameId = gameSessionDao.insertGameSession(newGameSession)

                // 4. Aggiorna lastUnfinishedGameId nelle UserPreferences
                userPreferencesRepository.updateLastUnfinishedGameId(gameId)

                // 5. Richiama onSuccess con la GameSession aggiornata con l'ID
                onSuccess(newGameSession.copy(id = gameId))

            }.onFailure { throwable ->
                onError(
                    Exception(
                        "Failed to get new sudoku puzzle from API: ${throwable.message}",
                        throwable
                    )
                )
            }

        } catch (e: Exception) {
            onError(e)
        }
    }

    override suspend fun getLatestUnfinishedGameSession(
        onSuccess: (GameSession) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val userPrefs = userPreferencesRepository.getUserPreferences().first()
            val lastGameId = userPrefs.lastUnfinishedGameId

            if (lastGameId != -1L) { // -1L indica nessun gioco non finito
                val gameSession = gameSessionDao.getGameSessionById(lastGameId).firstOrNull()
                if (gameSession != null && !gameSession.isSolved) {
                    onSuccess(gameSession)
                } else {
                    // Il gioco precedente è stato risolto o non trovato, rimuovi il riferimento
                    userPreferencesRepository.updateLastUnfinishedGameId(-1L)
                    onError(Exception("No unfinished game found or game was already solved."))
                }
            } else {
                onError(Exception("No last unfinished game ID found."))
            }
        } catch (e: Exception) {
            onError(e)
        }
    }


    override suspend fun updateGameSession(
        gameSession: GameSession,
        onSuccess: (GameSession) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val existingGameSessionResult: GameSession? = gameSessionDao.getGameSessionById(gameSession.id).firstOrNull()

            //val existingSession = gameSessionDao.getGameSessionById(gameSession.id)
            val wasJustSolved = (existingGameSessionResult?.isSolved == false) && gameSession.isSolved



            val finalGameSession: GameSession = if (wasJustSolved) {
                val endTime = System.currentTimeMillis()
                val score = calculateScore(
                    gameSession.durationSeconds!!,
                    gameSession.difficulty.toDifficultyLevel()
                )

                gameSession.copy(
                    endTimeMillis = endTime,
                    score = score,
                    datePlayedMillis = endTime // Data di completamento
                )
            } else {
                gameSession
            }
            gameSessionDao.updateGameSession(finalGameSession)

            if (wasJustSolved) {
                // Rimuovi l'ID del gioco non finito dalle preferenze utente
                userPreferencesRepository.updateLastUnfinishedGameId(-1L)

                // Aggiorna le statistiche generali e i tempi record
                val currentStats = userStatisticsDao.getUserStatistics().first() // Questo non dovrebbe più dare errore

                // Inizializza con valori di default se non esistono ancora statistiche (prima volta)
                val currentGamesPlayed = currentStats?.totalGamesPlayed ?: 0
                val currentGamesSolved = currentStats?.totalGamesSolved ?: 0
                val currentAverageTime = currentStats?.averageSolveTimeMillis ?: 0L
                val currentBestEasy = currentStats?.bestSolveTimeEasyMillis
                val currentBestMedium = currentStats?.bestSolveTimeMediumMillis
                val currentBestHard = currentStats?.bestSolveTimeHardMillis
                // Mappa a dominio, o crea nuovo

                val newTotalGamesPlayed = currentGamesPlayed + 1
                val newTotalGamesSolved = currentGamesSolved + 1
                val newAverageSolveTime =
                    if (newTotalGamesSolved > 0) {
                        (currentAverageTime * currentGamesSolved + (finalGameSession.durationSeconds ?: 0L) * 1000L) / newTotalGamesSolved
                    } else {
                        (finalGameSession.durationSeconds ?: 0L) * 1000L
                    }

                val newBestSolveTimeEasy = updateBestTime(currentBestEasy, finalGameSession.difficulty, DifficultyLevel.EASY, finalGameSession.durationSeconds)
                val newBestSolveTimeMedium = updateBestTime(currentBestMedium, finalGameSession.difficulty, DifficultyLevel.MEDIUM, finalGameSession.durationSeconds)
                val newBestSolveTimeHard = updateBestTime(currentBestHard, finalGameSession.difficulty, DifficultyLevel.HARD, finalGameSession.durationSeconds)

                val updatedStatsEntity = UserStatistics(
                    totalGamesPlayed = newTotalGamesPlayed,
                    totalGamesSolved = newTotalGamesSolved,
                    averageSolveTimeMillis = newAverageSolveTime,
                    bestSolveTimeEasyMillis = newBestSolveTimeEasy,
                    bestSolveTimeMediumMillis = newBestSolveTimeMedium,
                    bestSolveTimeHardMillis = newBestSolveTimeHard
                )
                userStatisticsDao.insertUserStatistics(updatedStatsEntity) // Usa insert con REPLACE strategy
                Log.d("GameRepositoryImpl", "User Statistics updated after game ${finalGameSession.id} solved.")
            }

            onSuccess(gameSession)
        } catch (e: Exception) {
            Log.e("GameRepositoryImpl", "Error updating game session: ${e.message}", e)
            onError(e)
        }
    }


    override suspend fun getUserStatistics(): Flow<UserStatistics> {
        return gameSessionDao.getAllGameSessions().map { gameSessions ->
            val solvedGames = gameSessions.filter { it.isSolved }

            var totalGamesPlayed = gameSessions.size // Tutti i giochi (risolti o meno)
            var totalGamesSolved = solvedGames.size

            var totalSolveTimeMillis = 0L
            val easySolveTimes = mutableListOf<Long>()
            val mediumSolveTimes = mutableListOf<Long>()
            val hardSolveTimes = mutableListOf<Long>()

            solvedGames.forEach { session ->
                session.durationSeconds?.let { duration ->
                    val durationMillis = duration * 1000L // Converti secondi in millisecondi
                    totalSolveTimeMillis += durationMillis

                    when (session.difficulty.toDifficultyLevel()) {
                        DifficultyLevel.EASY -> easySolveTimes.add(durationMillis)
                        DifficultyLevel.MEDIUM -> mediumSolveTimes.add(durationMillis)
                        DifficultyLevel.HARD -> hardSolveTimes.add(durationMillis)
                        else -> DifficultyLevel.HARD
                    }
                }
            }

            val averageSolveTimeMillis =
                if (totalGamesSolved > 0) totalSolveTimeMillis / totalGamesSolved else 0L

            UserStatistics(
                totalGamesPlayed = totalGamesPlayed,
                totalGamesSolved = totalGamesSolved,
                averageSolveTimeMillis = averageSolveTimeMillis,
                bestSolveTimeEasyMillis = easySolveTimes.minOrNull(), // minOrNull restituisce null se lista vuota
                bestSolveTimeMediumMillis = mediumSolveTimes.minOrNull(),
                bestSolveTimeHardMillis = hardSolveTimes.minOrNull()
            )
        }
    }


    // Funzione per calcolare il punteggio basata sul tempo e la difficoltà
    private fun calculateScore(elapsedTimeSeconds: Long, difficulty: DifficultyLevel): Int {
        val baseScore = 10000 // Punti base
        val timePenaltyFactor = 10 // Punti sottratti per ogni secondo
        val difficultyMultiplier = when (difficulty) {
            DifficultyLevel.EASY -> 1
            DifficultyLevel.MEDIUM -> 2
            DifficultyLevel.HARD -> 3
            else -> 1 // Fallback
        }

        val rawScore = baseScore - (elapsedTimeSeconds * timePenaltyFactor)
        return (rawScore * difficultyMultiplier).coerceAtLeast(0)
            .toInt() // Assicura che il punteggio non sia negativo
    }

    private fun updateBestTime(
        currentBest: Long?,
        gameDifficultyString: String,
        targetDifficulty: DifficultyLevel,
        gameDurationSeconds: Long?
    ): Long? {
        if (gameDurationSeconds == null) return currentBest
        val gameDurationMillis = gameDurationSeconds * 1000L

        return if (gameDifficultyString.toDifficultyLevel() == targetDifficulty) {
            if (currentBest == null || gameDurationMillis < currentBest) {
                gameDurationMillis
            } else {
                currentBest
            }
        } else {
            currentBest
        }
    }

}
