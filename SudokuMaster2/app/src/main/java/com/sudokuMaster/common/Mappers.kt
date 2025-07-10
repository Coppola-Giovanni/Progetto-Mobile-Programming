package com.sudokuMaster.common

import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.converter.SudokuGraphConverter
import com.sudokuMaster.data.model.GameSession
import com.sudokuMaster.domain.SudokuPuzzle

private val SudokuGraphConverter = SudokuGraphConverter()

fun SudokuPuzzle.toGameSession(existingId: Long = 0L, isSolved: Boolean = false, score: Int = 0): GameSession {
    // Serializza i LinkedHashMap in stringhe JSON usando il converter
    val initialGridJson = SudokuGraphConverter.fromGraph(this.initialGraph)
    val currentGridJson = SudokuGraphConverter.fromGraph(this.currentGraph)
    val solutionGridJson = SudokuGraphConverter.fromGraph(this.solutionGraph)

    val currentTimestamp = System.currentTimeMillis()

    return GameSession(
        id = existingId,
        difficulty = this.difficulty.name,
        initialGrid = initialGridJson,
        currentGrid = currentGridJson,
        startTimeMillis = currentTimestamp - (this.elapsedTime * 1000L),
        endTimeMillis = if (isSolved) currentTimestamp else null,
        durationSeconds = this.elapsedTime,
        score = score,
        isSolved = isSolved,
        datePlayedMillis = if (isSolved) currentTimestamp else 0L,
        solutionGrid = solutionGridJson
    )
}


fun GameSession.toSudokuPuzzle(): SudokuPuzzle {
    val boundary = 9 // Assumiamo 9x9 per tutti i Sudoku

    // Deserializza le stringhe JSON in LinkedHashMap usando il converter
    val initialGraph = SudokuGraphConverter.toGraph(this.initialGrid)
    val currentGraph = SudokuGraphConverter.toGraph(this.currentGrid)
    val solutionGraph = SudokuGraphConverter.toGraph(this.solutionGrid)

    return SudokuPuzzle(
        id = this.id,
        boundary = boundary,
        difficulty = DifficultyLevel.valueOf(this.difficulty),
        initialGraph = initialGraph,
        currentGraph = currentGraph,
        solutionGraph = solutionGraph,
        elapsedTime = this.durationSeconds ?: 0L
    )
}


fun String.toDifficultyLevel(): DifficultyLevel {
    return try {
        DifficultyLevel.valueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        DifficultyLevel.MEDIUM
    }
}

internal fun Long.toTime(): String {
    if( this >= 3600) return "+59:59"
    var minutes = ((this % 3600) / 60).toString()
    if(minutes.length == 1) minutes = "0$minutes"
    var seconds = (this % 60).toString()
    if (seconds.length == 1) seconds = "0$seconds"
    return String.format("$minutes:$seconds")
}

