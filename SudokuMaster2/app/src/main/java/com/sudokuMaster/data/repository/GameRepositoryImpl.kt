package com.sudokuMaster.data.repository

import com.sudokuMaster.common.toDifficultyLevel
import com.sudokuMaster.common.toGameSession
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.model.GameSession
import com.sudokuMaster.data.dao.GameSessionDao
import com.sudokuMaster.domain.UserStatistics
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.domain.GameRepositoryInterface
import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import com.sudokuMaster.logic.allSquaresAreFilled
import com.sudokuMaster.logic.isComplete
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Date // Import per Date
import java.util.LinkedList

class GameRepositoryImpl(
    private val gameSessionDao: GameSessionDao,
    private val userPreferencesRepository: UserPreferencesRepositoryInterface,
    private val sudokuRemoteDataSource: SudokuRemoteDataSource
) : GameRepositoryInterface {

    override suspend fun createNewGameAndSave(difficulty: DifficultyLevel): GameSession {
        // La logica try-catch per l'API è stata spostata internamente in SudokuRemoteDataSource
        // o gestita dal chiamante (ViewModel) con un try-catch esterno.
        // Qui ci aspettiamo che sudokuRemoteDataSource.getNewSudokuPuzzleData() gestisca i suoi errori
        // o propaghi un'eccezione se fallisce.
        val (initialGridData, actualDifficulty) = try {
            sudokuRemoteDataSource.getNewSudokuPuzzleData(difficulty).getOrThrow()
        } catch (e: Exception) {
            // Rilancia un'eccezione più specifica se necessario, o lascia che si propaghi
            throw IOException("Failed to fetch new Sudoku puzzle from API: ${e.message}", e)
        }

        val boundary = 9

        val initialGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()
        val currentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()

        for (row in 0 until boundary) {
            val initialRowList = LinkedList<SudokuNode>()
            val currentRowList = LinkedList<SudokuNode>()
            for (col in 0 until boundary) {
                val value = initialGridData[row][col]
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
                        color = value,
                        readOnly = isReadOnly
                    )
                )
            }
            initialGraph[row] = initialRowList
            currentGraph[row] = currentRowList
        }
        initialGraph.forEach { (_, list) -> list.sortBy { it.x } }
        currentGraph.forEach { (_, list) -> list.sortBy { it.x } }

        val newSudokuPuzzle = SudokuPuzzle(
            id = 0L,
            boundary = boundary,
            difficulty = actualDifficulty,
            initialGraph = initialGraph,
            currentGraph = currentGraph,
            elapsedTime = 0L
        )

        val newGameSession = newSudokuPuzzle.toGameSession(
            existingId = 0L,
            isSolved = false,
            score = 0
        ).copy(startTimeMillis = System.currentTimeMillis())

        val gameId = gameSessionDao.insertGameSession(newGameSession)

        userPreferencesRepository.updateLastUnfinishedGameId(gameId) // updateLastUnfinishedGameId restituisce Result<Unit>

        return newGameSession.copy(id = gameId) // Restituisci la sessione con l'ID reale
    }


    override suspend fun getLatestUnfinishedGameSession(): GameSession? { // Restituisce GameSession? o lancia
        val userPrefs = userPreferencesRepository.getUserPreferences().first()
        val lastGameId = userPrefs.lastUnfinishedGameId

        return if (lastGameId != -1L) {
            val gameSession = gameSessionDao.getGameSessionById(lastGameId).firstOrNull()
            if (gameSession != null && !gameSession.isSolved) {
                gameSession
            } else {
                userPreferencesRepository.updateLastUnfinishedGameId(-1L) // Pulisci l'ID se il gioco è risolto o non trovato
                null // Nessun gioco non finito valido trovato
            }
        } else {
            null // Nessun ID di gioco non finito salvato
        }
    }


    override suspend fun updateGameNodeAndSave(puzzle: SudokuPuzzle): GameSession {
        val updatedGameSession = puzzle.toGameSession(
            existingId = puzzle.id,
            isSolved = false,
            score = 0
        )

        val isPuzzleSolved = allSquaresAreFilled(puzzle) && puzzle.isComplete()

        val finalGameSession: GameSession
        if (isPuzzleSolved) {
            val endTime = System.currentTimeMillis()
            val initialGameSession = gameSessionDao.getGameSessionById(puzzle.id).firstOrNull()
                ?: throw IllegalStateException("Game session with ID ${puzzle.id} not found during update.")

            val duration = (endTime - initialGameSession.startTimeMillis) / 1000L // Durata in secondi
            val score = calculateScore(duration, puzzle.difficulty)

            finalGameSession = updatedGameSession.copy(
                endTimeMillis = endTime,
                durationSeconds = duration,
                score = score,
                isSolved = true,
                datePlayedMillis = endTime
            )
            userPreferencesRepository.updateLastUnfinishedGameId(-1L)
        } else {
            finalGameSession = updatedGameSession
        }

        gameSessionDao.updateGameSession(finalGameSession)
        return finalGameSession
    }

    override suspend fun updateGameSession(gameSession: GameSession) {
        gameSessionDao.updateGameSession(gameSession)
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

            val averageSolveTimeMillis = if (totalGamesSolved > 0) totalSolveTimeMillis / totalGamesSolved else 0L

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
            else-> 1 // Fallback
        }

        val rawScore = baseScore - (elapsedTimeSeconds * timePenaltyFactor)
        return (rawScore * difficultyMultiplier).coerceAtLeast(0).toInt() // Assicura che il punteggio non sia negativo
    }
}
