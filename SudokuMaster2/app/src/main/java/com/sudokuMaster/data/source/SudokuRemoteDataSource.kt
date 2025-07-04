package com.sudokuMaster.data.source

import com.google.gson.annotations.SerializedName
import com.sudokuMaster.data.DifficultyLevel
import retrofit2.http.GET
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun getNewSudokuPuzzleData(requestedDifficulty: DifficultyLevel): Result<Pair<List<List<Int>>, DifficultyLevel>> {
        return withContext(Dispatchers.IO) { // Esegue la chiamata di rete su un thread I/O
            try {
                val response = apiService.getNewSudoku()
                val initialGridData = response.newBoard.grids.firstOrNull()?.value
                    ?: throw IllegalStateException("API response did not contain grid data.")
                val apiDifficultyString = response.newBoard.grids.firstOrNull()?.difficulty
                    ?: "DIFFICULTY_UNSPECIFIED" // Default se non specificato

                // Mappa la stringa della difficoltà API al tuo enum DifficultyLevel
                val actualDifficulty = try {
                    DifficultyLevel.valueOf(apiDifficultyString.uppercase())
                } catch (e: IllegalArgumentException) {
                    DifficultyLevel.DIFFICULTY_UNSPECIFIED
                }

                Result.success(Pair(initialGridData, actualDifficulty))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
