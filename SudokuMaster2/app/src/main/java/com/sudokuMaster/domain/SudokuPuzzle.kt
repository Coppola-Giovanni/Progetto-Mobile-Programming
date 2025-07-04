package com.sudokuMaster.domain

import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.logic.buildNewSudoku
import java.util.*
import kotlin.collections.LinkedHashMap

data class SudokuPuzzle(
    val boundary: Int,
    val difficulty: DifficultyLevel,
    val graph: LinkedHashMap<Int, LinkedList<SudokuNode>>
    = buildNewSudoku(difficulty).graph,
    var elapsedTime: Long = 0L
){
    fun getValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = graph
}
