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
    val solutionGraph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    var elapsedTime: Long = 0L
){
    //Funzioni di utility mai utilizzate nell'attuale implementazione a causa della mancanza di tempo, ma che vedranno la luce in versioni future
    fun getCurrentValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = currentGraph
    fun getInitialValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = initialGraph
    fun getSolutionValue(): LinkedHashMap<Int, LinkedList<SudokuNode>> = solutionGraph

}

