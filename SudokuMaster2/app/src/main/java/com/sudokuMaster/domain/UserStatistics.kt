package com.sudokuMaster.domain

data class UserStatistics(
    val totalGamesPlayed: Int = 0,
    val totalGamesSolved: Int = 0,
    val averageSolveTimeMillis: Long = 0L,
    val bestSolveTimeEasyMillis: Long? = null,
    val bestSolveTimeMediumMillis: Long? = null,
    val bestSolveTimeHardMillis: Long?
)
