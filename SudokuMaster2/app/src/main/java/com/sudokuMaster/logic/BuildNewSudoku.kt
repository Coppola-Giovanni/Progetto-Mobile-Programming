
package com.sudokuMaster.logic

import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import java.util.LinkedHashMap
import java.util.LinkedList
import kotlin.random.Random

/**
 * Costruisce un oggetto SudokuPuzzle di dominio a partire da una griglia di numeri (List<List<Int>>)
 * e una difficoltà specificata.
 * Questa funzione non effettua chiamate di rete.
 * @param initialGridData La griglia di numeri (0 per celle vuote) da cui costruire il puzzle.
 * @param difficulty La difficoltà del puzzle.
 * @return Un SudokuPuzzle di dominio.
 */
fun buildNewSudokuPuzzleFromGrid(
    initialGridData: List<List<Int>>,
    difficulty: DifficultyLevel
): SudokuPuzzle {
    val initialGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()
    val currentGraph = LinkedHashMap<Int, LinkedList<SudokuNode>>()


    for (row in 0 until 9) {
        val initialRowList = LinkedList<SudokuNode>()
        val currentRowList = LinkedList<SudokuNode>()
        for (col in 0 until 9) {
            val value = initialGridData[row][col] // Usa il valore dalla griglia fornita
            initialRowList.add(
                SudokuNode(
                    x = col, // X è la colonna
                    y = row, // Y è la riga
                    color = value,
                    readOnly = value != 0 // Se il valore iniziale non è 0, è una cella di partenza

                )
            )

            currentRowList.add(
                SudokuNode(
                    x = col,
                    y = row,
                    color = value,
                    readOnly = value != 0 // Usa lo stesso criterio per readOnly
                )
            )
        }
        initialGraph[row] = initialRowList
        currentGraph[row] = currentRowList
    }

    // Assumiamo che il boundary sia 9 per un Sudoku 9x9 standard
    // Il campo `id` e `elapsedTime` saranno gestiti dal repository Room
    return SudokuPuzzle(
        id = 0L,
        boundary = 9,
        difficulty = difficulty,
        initialGraph = initialGraph,
        currentGraph = currentGraph,
        elapsedTime = 0L)
}

