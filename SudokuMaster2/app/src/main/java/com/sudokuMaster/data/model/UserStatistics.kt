package com.sudokuMaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_statistics")
data class UserStatistics(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // Ci sarà sempre una sola riga per le statistiche complessive
    val totalGamesPlayed: Int,
    val totalGamesSolved: Int,
    val averageSolveTimeMillis: Long,
    val bestSolveTimeEasyMillis: Long?,
    val bestSolveTimeMediumMillis: Long?,
    val bestSolveTimeHardMillis: Long?
)
