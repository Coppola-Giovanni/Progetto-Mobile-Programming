package com.sudokuMaster.domain

import java.util.*
import kotlin.collections.LinkedHashMap

data class SudokuPuzzle(
    val difficulty: Difficulty,
    val graph: LinkedHashMap<Int, LinkedList<SudokuNode>>
    = buildNewSudoku(difficulty).graph,
    val elapsedTime: Long = 0L,
) {
    fun getValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = graph
}

