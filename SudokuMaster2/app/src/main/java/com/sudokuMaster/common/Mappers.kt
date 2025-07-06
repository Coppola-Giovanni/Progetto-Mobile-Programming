package com.sudokuMaster.common

import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.model.GameSession
import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import java.util.LinkedHashMap
import java.util.LinkedList

fun SudokuPuzzle.toGameSession(existingId: Long = 0L, isSolved: Boolean = false, score: Int = 0): GameSession {
    val initialGridString = StringBuilder()
    this.initialGraph.forEach { (_, nodes) ->
        nodes.sortBy { it.x }
        nodes.forEach { node ->
            initialGridString.append(node.color)
        }
    }

    val currentGridString = StringBuilder()
    this.currentGraph.forEach { (_, nodes) ->
        nodes.sortBy { it.x }
        nodes.forEach { node ->
            currentGridString.append(node.color)
        }
    }

    val currentTimestamp = System.currentTimeMillis()

    return GameSession(
        id = existingId, // 0 per un nuovo gioco
        difficulty = this.difficulty.name,
        initialGrid = initialGridString.toString(),
        currentGrid = currentGridString.toString(),
        startTimeMillis = currentTimestamp - (this.elapsedTime * 1000L), // Stima del tempo di inizio
        endTimeMillis = if (isSolved) currentTimestamp else null,
        durationSeconds = this.elapsedTime, // Tempo trascorso in secondi
        score = score,
        isSolved = isSolved,
        datePlayedMillis = if (isSolved) currentTimestamp else 0L // Data di gioco solo se risolto
    )
}

fun GameSession.toSudokuPuzzle(): SudokuPuzzle {
    val boundary = 9

    val initialGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()
    val currentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()

    // Ricostruisce initialGraph e currentGraph dalle stringhe della griglia
    for (row in 0 until boundary) {
        val initialRowList = LinkedList<SudokuNode>()
        val currentRowList = LinkedList<SudokuNode>()
        for (col in 0 until boundary) {
            val initialValue = this.initialGrid[row * boundary + col].toString().toInt()
            val currentValue = this.currentGrid[row * boundary + col].toString().toInt()

            initialRowList.add(
                SudokuNode(
                    x = col,
                    y = row,
                    color = initialValue,
                    readOnly = initialValue != 0
                )
            )
            currentRowList.add(
                SudokuNode(
                    x = col,
                    y = row,
                    color = currentValue,
                    readOnly = initialValue != 0
                )
            )
        }
        initialGraph[row] = initialRowList
        currentGraph[row] = currentRowList
    }


    initialGraph.forEach { (_, list) -> list.sortBy { it.x } }
    currentGraph.forEach { (_, list) -> list.sortBy { it.x } }

    return SudokuPuzzle(
        id = this.id,
        boundary = boundary,
        difficulty = DifficultyLevel.valueOf(this.difficulty),
        initialGraph = initialGraph,
        currentGraph = currentGraph,
        elapsedTime = this.durationSeconds ?: 0L
    )
}

fun String.toDifficultyLevel(): DifficultyLevel {
    return try {
        DifficultyLevel.valueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        DifficultyLevel.MEDIUM // Fallback a MEDIUM o un altro default sensato
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

