package com.sudokuMaster.domain

import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.logic.sqrt
import java.util.*
import kotlin.collections.LinkedHashMap

data class SudokuPuzzle(
    val id: Long= 0L,
    val boundary: Int,
    val difficulty: DifficultyLevel,
    val initialGraph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    val currentGraph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    val solutionGraph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    var elapsedTime: Long = 0L
){
    fun getEmptyNodes(): List<SudokuNode> {
        return currentGraph.values.flatten().filter { it.color == 0 && !it.readOnly }
    }

    fun getPossibleCandidates(node: SudokuNode): Set<Int> {
        if (node.color != 0 || node.readOnly) return emptySet()

        val boundary = this.boundary
        val possibleCandidates = (1..boundary).toMutableSet()

        // Rimuove i numeri presenti nella stessa riga
        for (x in 0 until boundary) {
            val cell = currentGraph[node.y]?.find { it.x == x }
            if (cell != null && cell.color != 0) {
                possibleCandidates.remove(cell.color)
            }
        }

        // Rimuove i numeri presenti nella stessa colonna
        for (y in 0 until boundary) {
            val cell = currentGraph[y]?.find { it.x == node.x }
            if (cell != null && cell.color != 0) {
                possibleCandidates.remove(cell.color)
            }
        }

        // Rimuove i numeri presenti nella stessa sottogriglia 3x3
        val subgridSize = boundary.sqrt
        val startRow = (node.y / subgridSize) * subgridSize
        val startCol = (node.x / subgridSize) * subgridSize

        for (r in startRow until startRow + subgridSize) {
            for (c in startCol until startCol + subgridSize) {
                val cell = currentGraph[r]?.find { it.x == c }
                if (cell != null && cell.color != 0) {
                    possibleCandidates.remove(cell.color)
                }
            }
        }
        return possibleCandidates
    }
}

