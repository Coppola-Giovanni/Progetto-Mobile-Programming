package com.sudokuMaster.domain

import com.sudokuMaster.logic.buildNewSudoku
import java.util.*
import kotlin.collections.LinkedHashMap

data class SudokuPuzzle(
    val difficulty: Difficulty,
    val graph: LinkedHashMap<Int, LinkedList<SudokuNode>>
    =buildNewSudoku(context)
    val elapsedTime: Long = 0L,
) {
    fun getValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = graph
}

