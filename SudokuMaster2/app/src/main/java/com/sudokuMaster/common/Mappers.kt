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
        nodes.sortBy { it.x } // Assicurati che i nodi siano ordinati per X per la stringa
        nodes.forEach { node ->
            initialGridString.append(node.color)
        }
    }

    val currentGridString = StringBuilder()
    this.currentGraph.forEach { (_, nodes) ->
        nodes.sortBy { it.x } // Assicurati che i nodi siano ordinati per X per la stringa
        nodes.forEach { node ->
            currentGridString.append(node.color)
        }
    }

    // La logica di startTimeMillis/endTimeMillis/duration_seconds sarà gestita dal repository
    // durante il salvataggio/aggiornamento. Qui passiamo solo elapsedTime
    val currentTimestamp = System.currentTimeMillis()

    return GameSession(
        id = existingId, // L'ID esistente se stiamo aggiornando, 0 per un nuovo gioco
        difficulty = this.difficulty.name, // Converti l'enum in String
        initialGrid = initialGridString.toString(),
        currentGrid = currentGridString.toString(),
        startTimeMillis = currentTimestamp - (this.elapsedTime * 1000L), // Stima del tempo di inizio
        endTimeMillis = if (isSolved) currentTimestamp else null, // Imposta solo se risolto
        durationSeconds = this.elapsedTime, // Tempo trascorso in secondi
        score = score, // Punti, aggiornati dal repository alla risoluzione
        isSolved = isSolved, // Stato di risoluzione
        datePlayedMillis = if (isSolved) currentTimestamp else 0L // Data di gioco solo se risolto
    )
}

fun GameSession.toSudokuPuzzle(): SudokuPuzzle {
    val boundary = 9 // Assumiamo Sudoku 9x9

    val initialGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()
    val currentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()

    // Ricostruisci initialGraph e currentGraph dalle stringhe della griglia
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
                    readOnly = initialValue != 0 // readOnly basato sull'initialValue
                )
            )
            currentRowList.add(
                SudokuNode(
                    x = col,
                    y = row,
                    color = currentValue,
                    readOnly = initialValue != 0 // readOnly basato sull'initialValue
                )
            )
        }
        initialGraph[row] = initialRowList
        currentGraph[row] = currentRowList
    }

    // Ordina i nodi all'interno di ogni riga per x per garantire la coerenza
    initialGraph.forEach { (_, list) -> list.sortBy { it.x } }
    currentGraph.forEach { (_, list) -> list.sortBy { it.x } }

    return SudokuPuzzle(
        id = this.id,
        boundary = boundary,
        difficulty = DifficultyLevel.valueOf(this.difficulty), // Converti String in Enum
        initialGraph = initialGraph,
        currentGraph = currentGraph,
        elapsedTime = this.durationSeconds ?: 0L // Utilizza durationSeconds per elapsedTime
    )
}

// Estensione per convertire String in DifficultyLevel (utile altrove, se necessario)
fun String.toDifficultyLevel(): DifficultyLevel {
    return try {
        DifficultyLevel.valueOf(this.uppercase())
    } catch (e: IllegalArgumentException) {
        DifficultyLevel.MEDIUM // Fallback a MEDIUM o un altro default sensato
    }
}

