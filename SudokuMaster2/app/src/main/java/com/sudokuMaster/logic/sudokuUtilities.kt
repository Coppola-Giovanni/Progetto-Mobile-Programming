package com.sudokuMaster.logic

import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import java.util.LinkedList


internal val Int.sqrt: Int
    get() = kotlin.math.sqrt(this.toDouble()).toInt()

internal fun SudokuPuzzle.isComplete(): Boolean {
    return when {
        !this.isValid() -> false // Chiama la funzione di estensione
        !allSquaresAreFilled(this) -> false
        else -> true
    }
}

internal fun SudokuPuzzle.isValid(): Boolean {
    return when {
        rowsAreInvalid(this) -> false
        columnsAreInvalid(this) -> false
        subgridsAreInvalid(this) -> false
        else -> true
    }
}


internal fun rowsAreInvalid(puzzle: SudokuPuzzle): Boolean {

    (0 until puzzle.boundary).forEach { row ->
        val nodeList = getNodesByRow(puzzle.currentGraph, row)
        (1..puzzle.boundary).forEach { value ->
            val occurrences = nodeList.count { it.color == value && it.color != 0 }
            if (occurrences > 1) return true
        }
    }
    return false
}


internal fun columnsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    (0 until puzzle.boundary).forEach { col ->
        val nodeList = getNodesByColumn(puzzle.currentGraph, col)
        (1..puzzle.boundary).forEach { value ->
            val occurrences = nodeList.count { it.color == value && it.color != 0 }
            if (occurrences > 1) return true
        }
    }
    return false
}

internal fun subgridsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    val boundary = puzzle.boundary
    val interval = boundary.sqrt

    (0 until interval).forEach { subgridRowIndex ->
        (0 until interval).forEach { subgridColIndex ->
            val startNodeX = subgridColIndex * interval
            val startNodeY = subgridRowIndex * interval
            val subgridNodes = getNodesBySubgrid(
                puzzle.currentGraph,
                startNodeX,
                startNodeY,
                boundary
            )

            // Verifica le occorrenze dei valori all'interno di questa singola sottogriglia
            (1..boundary).forEach { value ->
                val occurrences = subgridNodes.count { it.color == value && it.color != 0 } // Aggiunto check != 0
                if (occurrences > 1) return true // Se un numero > 0 appare più di una volta, la sottogriglia è invalida
            }
        }
    }
    return false
}


internal fun allSquaresAreFilled(puzzle: SudokuPuzzle): Boolean {
    // Itera su tutte le LinkedList (righe) e su tutti i nodi all'interno di ciascuna riga
    return puzzle.currentGraph.values.all { rowList ->
        rowList.all { node -> node.color != 0 }
    }
}


internal fun getNodesByColumn(
    graph: LinkedHashMap<Int, LinkedList<SudokuNode>>, x: Int
): List<SudokuNode> {
    val columnNodes = mutableListOf<SudokuNode>()
    // Itera su tutte le LinkedList di SudokuNode (ogni LinkedList è una riga)
    graph.values.forEach { rowList ->
        // Per ogni riga, trova il nodo con la coordinata x specificata
        rowList.find { node -> node.x == x }?.let { node ->
            columnNodes.add(node)
        }
    }
    return columnNodes
}

internal fun getNodesByRow(
    graph: LinkedHashMap<Int, LinkedList<SudokuNode>>, y: Int
): List<SudokuNode> {
    return graph[y]?.toList() ?: emptyList()
}

internal fun getNodesBySubgrid(
    graph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    nodeX: Int, nodeY: Int, boundary: Int
): List<SudokuNode> {
    val subgridNodes = mutableListOf<SudokuNode>()
    val subgridSize = boundary.sqrt
    val startRow = (nodeY / subgridSize) * subgridSize
    val endRow = startRow + subgridSize -1

    val startCol = (nodeX / subgridSize) * subgridSize
    val endCol = startCol + subgridSize - 1

    // Itera su tutte le righe che fanno parte di questa sottogriglia
    for (row in startRow..endRow) {
        graph[row]?.forEach { node ->
            // Aggiunge solo i nodi che rientrano nelle colonne della sottogriglia
            if (node.x in startCol..endCol) {
                subgridNodes.add(node)
            }
        }
    }
    return subgridNodes
}


