
package com.sudokuMaster.logic

import com.sudokuMaster.data.DifficultyLevel
import java.util.LinkedHashMap
import java.util.LinkedList
import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle

fun buildNewSudoku(difficulty: DifficultyLevel): SudokuPuzzle {
    val graph = LinkedHashMap<Int, LinkedList<SudokuNode>>()


    for (row in 0 until 9) {
        val rowList = LinkedList<SudokuNode>()
        for (col in 0 until 9) {
            rowList.add(SudokuNode(row, col, 0, true))
        }
        graph[row] = rowList
    }

    val solvedGrid = generateFullSudokuGrid()


    val puzzleGrid = removeCellsBasedOnDifficulty(solvedGrid, difficulty)


    for (row in 0 until 9) {
        for (col in 0 until 9) {
            val value = puzzleGrid[row][col]
            graph[row]?.set(col, SudokuNode(row, col,0, true))
        }
    }

    return SudokuPuzzle(9, difficulty, graph)
}


fun generateFullSudokuGrid(): Array<IntArray> {
    return arrayOf(
        intArrayOf(5, 3, 4, 6, 7, 8, 9, 1, 2),
        intArrayOf(6, 7, 2, 1, 9, 5, 3, 4, 8),
        intArrayOf(1, 9, 8, 3, 4, 2, 5, 6, 7),
        intArrayOf(8, 5, 9, 7, 6, 1, 4, 2, 3),
        intArrayOf(4, 2, 6, 8, 5, 3, 7, 9, 1),
        intArrayOf(7, 1, 3, 9, 2, 4, 8, 5, 6),
        intArrayOf(9, 6, 1, 5, 3, 7, 2, 8, 4),
        intArrayOf(2, 8, 7, 4, 1, 9, 6, 3, 5),
        intArrayOf(3, 4, 5, 2, 8, 6, 1, 7, 9)
    )
}


fun removeCellsBasedOnDifficulty(grid: Array<IntArray>, difficulty: DifficultyLevel): Array<IntArray> {
    val cellsToRemove = when (difficulty) {
        DifficultyLevel.EASY -> 35
        DifficultyLevel.MEDIUM -> 45
        DifficultyLevel.HARD -> 55
        else -> 55
    }

    val puzzle = grid.map { it.copyOf() }.toTypedArray()
    val rand = java.util.Random()
    var removed = 0

    while (removed < cellsToRemove) {
        val row = rand.nextInt(9)
        val col = rand.nextInt(9)
        if (puzzle[row][col] != 0) {
            puzzle[row][col] = 0
            removed++
        }
    }

    return puzzle
}