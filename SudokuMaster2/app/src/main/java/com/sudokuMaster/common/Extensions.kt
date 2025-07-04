package com.sudokuMaster.common

import android.app.Activity
import android.widget.Toast
import com.SudokuMaster.R
import com.sudokuMaster.data.DifficultyLevel


internal fun Activity.makeToast(message:String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_LONG
    ).show()
}

internal fun Long.toTime(): String {
    if( this >= 3600) return "+59:59"
    var minutes = ((this % 3600) / 60).toString()
    if(minutes.length == 1) minutes = "0$minutes"
    var seconds = (this % 60).toString()
    if (seconds.length == 1) seconds = "0$seconds"
    return String.format("$minutes:$seconds")
}

internal val DifficultyLevel.toLocalizedResource: Int
    get() {
        return when (this) {
            DifficultyLevel.EASY -> R.string.easy
            DifficultyLevel.MEDIUM -> R.string.medium
            DifficultyLevel.HARD -> R.string.hard
            else -> R.string.hard
        }
    }
fun DifficultyLevel.getModifier(): Double {
    return when (this) {
        DifficultyLevel.EASY -> 0.50
        DifficultyLevel.MEDIUM -> 0.44
        DifficultyLevel.HARD -> 0.38
        else -> 0.38
    }
}

fun DifficultyLevel.getApiParam(): String {
    return when (this) {
        DifficultyLevel.EASY -> "easy"
        DifficultyLevel.MEDIUM -> "medium"
        DifficultyLevel.HARD -> "hard"
        else -> "hard"
    }
}
