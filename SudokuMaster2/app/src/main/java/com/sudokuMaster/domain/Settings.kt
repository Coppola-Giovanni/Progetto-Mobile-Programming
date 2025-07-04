package com.sudokuMaster.domain

import com.sudokuMaster.data.AppTheme
import com.sudokuMaster.data.DifficultyLevel

data class Settings(
    val boundary: Int,
    val difficulty: DifficultyLevel,
    val appTheme: AppTheme,
    val defaultDifficulty: DifficultyLevel,
    val soundEnabled: Boolean,
    //val showTutorial: Boolean,              not implemented yet!

    val lastUnfinishedGameId: Int,
    val lastAccessTimestamp: Int
)

