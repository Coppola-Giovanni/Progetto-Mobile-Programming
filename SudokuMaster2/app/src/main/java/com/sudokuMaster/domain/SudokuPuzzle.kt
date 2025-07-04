package com.sudokuMaster.domain

import com.sudokuMaster.data.DifficultyLevel
import java.util.*
import kotlin.collections.LinkedHashMap

data class SudokuPuzzle(
    val id: Long= 0L,
    val boundary: Int,
    val difficulty: DifficultyLevel,
    val initialGraph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    val currentGraph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    var elapsedTime: Long = 0L
){
    fun getCurrentValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = currentGraph
    fun getInitialValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = initialGraph
}

