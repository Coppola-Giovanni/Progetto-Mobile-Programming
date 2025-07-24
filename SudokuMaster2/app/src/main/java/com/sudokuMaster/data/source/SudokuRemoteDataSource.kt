package com.sudokuMaster.data.source

import com.google.gson.annotations.SerializedName
import com.sudokuMaster.data.DifficultyLevel
import retrofit2.http.GET
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay // Importa delay per le pause tra i tentativi

// --- 1. Modelli di Risposta API (Data Transfer Objects - DTO) ---
// Questi sono per il parsing JSON dall'API
data class ApiResponse(
    @SerializedName("newboard") val newBoard: NewBoardResponse
)

data class NewBoardResponse(
    @SerializedName("grids") val grids: List<GridResponse>
)

data class GridResponse(
    @SerializedName("value") val value: List<List<Int>>, // La griglia Sudoku
    @SerializedName("solution") val solution: List<List<Int>>,
    @SerializedName("difficulty") val difficulty: String // Difficoltà come stringa
)

// --- 2. Interfaccia del Servizio Retrofit ---
interface SudokuApiService {
    @GET("api/dosuku")
    // L'API vercel.app non supporta un parametro di query per la difficoltà in questo endpoint.
    // Se fosse supportato, sarebbe: @Query("query") difficulty: String
    suspend fun getNewSudoku(): ApiResponse
}

// --- 3. Implementazione del Remote Data Source ---
class SudokuRemoteDataSource(private val apiService: SudokuApiService) {

    // Numero massimo di tentativi per trovare la difficoltà desiderata
    private val MAX_RETRIES = 20

    // Ritardo tra i tentativi in millisecondi
    private val RETRY_DELAY_MS = 10L

    suspend fun getNewSudokuPuzzleData(requestedDifficulty: DifficultyLevel): Result<Triple<List<List<Int>>, List<List<Int>>, DifficultyLevel>> {
        return withContext(Dispatchers.IO) { // Esegue la chiamata di rete su un thread I/O
            var currentRetries = 0
            while (currentRetries < MAX_RETRIES) {
                try {
                    val response = apiService.getNewSudoku()
                    val gridResponse = response.newBoard.grids.firstOrNull()
                        ?: throw IllegalStateException("API response did not contain grid data.")

                    val initialGridData = gridResponse.value
                    val solutionGridData = gridResponse.solution
                    val apiDifficultyString = gridResponse.difficulty
                        ?: "DIFFICULTY_UNSPECIFIED"

                    val actualDifficulty = try {
                        DifficultyLevel.valueOf(apiDifficultyString.uppercase())
                    } catch (e: IllegalArgumentException) {
                        DifficultyLevel.DIFFICULTY_UNSPECIFIED
                    }

                    // Confronta la difficoltà ottenuta dall'API con la difficoltà richiesta
                    if (actualDifficulty == requestedDifficulty || requestedDifficulty == DifficultyLevel.DIFFICULTY_UNSPECIFIED) {
                        return@withContext Result.success(Triple(initialGridData, solutionGridData, actualDifficulty))
                    } else {
                        // Se la difficoltà non corrisponde, riprova
                        currentRetries++
                        delay(RETRY_DELAY_MS) // Attende prima di un nuovo tentativo
                    }
                } catch (e: Exception) {
                    // Gestisce eventuali errori di rete o API, e riprova
                    currentRetries++
                    delay(RETRY_DELAY_MS) // Attende prima di un nuovo tentativo
                    if (currentRetries >= MAX_RETRIES) {
                        return@withContext Result.failure(e)
                    }
                }
            }
            // Se si raggiungono i tentativi massimi senza trovare la difficoltà desiderata
            Result.failure(Exception("Could not find a Sudoku puzzle with the requested difficulty after $MAX_RETRIES attempts."))
        }
    }
}