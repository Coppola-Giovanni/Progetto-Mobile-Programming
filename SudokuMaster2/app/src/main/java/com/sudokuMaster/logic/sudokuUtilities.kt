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
    // Itera su ogni riga (0-based)
    (0 until puzzle.boundary).forEach { row ->
        val nodeList = getNodesByRow(puzzle.currentGraph, row)
        // Itera sui possibili valori (da 1 al boundary)
        (1..puzzle.boundary).forEach { value ->
            // Conta le occorrenze di 'value' (non zero)
            val occurrences = nodeList.count { it.color == value && it.color != 0 }
            if (occurrences > 1) return true
        }
    }
    return false
}


internal fun columnsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    // Itera su ogni colonna (0-based)
    (0 until puzzle.boundary).forEach { col ->
        val nodeList = getNodesByColumn(puzzle.currentGraph, col)
        // Itera sui possibili valori (da 1 al boundary)
        (1..puzzle.boundary).forEach { value ->
            // Conta le occorrenze di 'value' (non zero)
            val occurrences = nodeList.count { it.color == value && it.color != 0 }
            if (occurrences > 1) return true
        }
    }
    return false
}

internal fun subgridsAreInvalid(puzzle: SudokuPuzzle): Boolean {
    val boundary = puzzle.boundary // Usa puzzle.boundary per chiarezza, è 9
    val interval = boundary.sqrt // Sarà 3 per un 9x9

    // Itera attraverso l'inizio di ogni sottogriglia
    // (xIndex, yIndex) rappresenta l'indice della sottogriglia, non del nodo
    (0 until interval).forEach { subgridRowIndex -> // Indice della riga della sottogriglia (0, 1, 2)
        (0 until interval).forEach { subgridColIndex -> // Indice della colonna della sottogriglia (0, 1, 2)
            // Calcola le coordinate (0-based) del primo nodo di questa sottogriglia
            val startNodeX = subgridColIndex * interval
            val startNodeY = subgridRowIndex * interval

            // Ottieni tutti i nodi per la sottogriglia a cui appartiene il nodo (startNodeX, startNodeY)
            // La funzione getNodesBySubgrid ora calcola l'intera sottogriglia a partire da un qualsiasi nodo al suo interno.
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
    // La mappa è già indicizzata per riga (y). Assumiamo che y sia 0-based.
    // Se usi 1-based nell'interfaccia, converti y-1.
    // Dalla buildNewSudokuPuzzleFromGrid, le righe sono aggiunte con chiave 'row' da 0 a 8.
    return graph[y]?.toList() ?: emptyList() // Converti in List immutabile e gestisci il caso nullo
}

internal fun getNodesBySubgrid(
    graph: LinkedHashMap<Int, LinkedList<SudokuNode>>,
    // Questi x e y dovrebbero essere le coordinate (0-based) di un nodo all'interno della sottogriglia
    // es. per il nodo (0,0), (0,1) ecc.
    // Se in ingresso ricevi x e y dell'angolo superiore sinistro (1-based), devi adattare
    // Ma è più robusto calcolare la subgrid a cui appartiene un dato nodo (x,y)
    // Riscrivo questa funzione per prendere un x e y di QUALSIASI nodo e trovare la sua sottogriglia.
    nodeX: Int, nodeY: Int, boundary: Int
): List<SudokuNode> {
    val subgridNodes = mutableListOf<SudokuNode>()
    val subgridSize = boundary.sqrt // Per Sudoku 9x9, sqrt è 3

    // Calcola la riga e la colonna di inizio della sottogriglia a cui appartiene (nodeX, nodeY)
    val startRow = (nodeY / subgridSize) * subgridSize
    val endRow = startRow + subgridSize -1

    val startCol = (nodeX / subgridSize) * subgridSize
    val endCol = startCol + subgridSize - 1

    // Itera su tutte le righe che fanno parte di questa sottogriglia
    for (row in startRow..endRow) {
        graph[row]?.forEach { node ->
            // Aggiungi solo i nodi che rientrano nelle colonne della sottogriglia
            if (node.x in startCol..endCol) {
                subgridNodes.add(node)
            }
        }
    }
    return subgridNodes
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
    val boundary = this.boundary // Usa puzzle.boundary, che è 9
    (0 until boundary).forEach { y -> // Itera sulle righe da 0 a 8
        val row = (0 until boundary).map { x -> // Itera sulle colonne da 0 a 8
            // Accedi alla LinkedList della riga 'y' e trova il nodo con x 'x'
            currentGraph[y]?.find { node -> node.x == x }?.color?.toString() ?: "."
        }.joinToString(" ")
        println(row)
    }
}

