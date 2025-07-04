package com.sudokuMaster.logic

import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import com.sudokuMaster.domain.getHash
import java.util.LinkedList
import kotlin.math.sqrt


internal val Int.sqrt: Int
    get() = kotlin.math.sqrt(this.toDouble()).toInt()

internal fun puzzleIsComplete(puzzle: SudokuPuzzle): Boolean {
    return when {
        !puzzleIsValid(puzzle) -> false
        !allSquaresAreFilled(puzzle) -> false
        else -> true
    }
}

internal fun puzzleIsValid(puzzle: SudokuPuzzle): Boolean {
    return when {
        rowsAreInvalid(puzzle) -> false
        columnsAreInvalid(puzzle) -> false
        subgridsAreInvalid(puzzle) -> false
        else -> true
    }
}

internal fun rowsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    (1..puzzle.currentGraph.size.sqrt).forEach { row ->
        val nodeList = getNodesByRow(puzzle.currentGraph, row)
        (1..puzzle.currentGraph.size.sqrt).forEach { value ->
            val occurrences = nodeList.count { it.color == value }
            if (occurrences > 1) return true
        }
    }
    return false
}

internal fun columnsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    (1..puzzle.currentGraph.size.sqrt).forEach { col ->
        val nodeList = getNodesByColumn(puzzle.currentGraph, col)
        (1..puzzle.currentGraph.size.sqrt).forEach { value ->
            val occurrences = nodeList.count { it.color == value }
            if (occurrences > 1) return true
        }
    }
    return false
}

internal fun subgridsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    val boundary = puzzle.currentGraph.size.sqrt
    val interval = boundary.sqrt

    (0 until interval).forEach { xIndex ->
        (0 until interval).forEach { yIndex ->
            val subgridNodes = getNodesBySubgrid(
                puzzle.currentGraph,
                xIndex * interval + 1,
                yIndex * interval + 1,
                boundary
            )
            (1..boundary).forEach { value ->
                val occurrences = subgridNodes.count { it.color == value }
                if (occurrences > 1) return true
            }
        }
    }
    return false
}

internal fun allSquaresAreFilled(puzzle: SudokuPuzzle): Boolean {
    return puzzle.currentGraph.values.all { it.first.color != 0 }
}

internal fun getNodesByColumn(
    graph: LinkedHashMap<Int, LinkedList<SudokuNode>>, x: Int
): List<SudokuNode> {
    return graph.values.map { it.first }.filter { it.x == x }
}

internal fun getNodesByRow(
    graph: LinkedHashMap<Int, LinkedList<SudokuNode>>, y: Int
): List<SudokuNode> {
    return graph.values.map { it.first }.filter { it.y == y }
}

internal fun getNodesBySubgrid(
    graph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    x: Int, y: Int, boundary: Int
): List<SudokuNode> {
    val edgeList = mutableListOf<SudokuNode>()
    val iMaxX = getIntervalMax(boundary, x)
    val iMaxY = getIntervalMax(boundary, y)
    val sqrt = boundary.sqrt

    ((iMaxX - sqrt + 1)..iMaxX).forEach { xIndex ->
        ((iMaxY - sqrt + 1)..iMaxY).forEach { yIndex ->
            graph[getHash(xIndex, yIndex)]?.first?.let { edgeList.add(it) }
        }
    }

    return edgeList
}

internal fun getIntervalMax(boundary: Int, target: Int): Int {
    val interval = boundary.sqrt
    (1..interval).forEach { index ->
        if (interval * index >= target && target > (interval * index - interval)) {
            return index * interval
        }
    }
    return boundary
}

internal fun SudokuPuzzle.print() {
    val boundary = currentGraph.size.sqrt
    (1..boundary).forEach { y ->
        val row = (1..boundary).map { x ->
            currentGraph[getHash(x, y)]?.first?.color?.toString() ?: "."
        }.joinToString(" ")
        println(row)
    }
}
