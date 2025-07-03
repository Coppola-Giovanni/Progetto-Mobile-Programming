
package com.sudokuMaster.logic

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.sudokuMaster.domain.Difficulty
import com.sudokuMaster.domain.SudokuNode
import com.sudokuMaster.domain.SudokuPuzzle
import java.util.*


fun buildNewSudoku(
    context: Context,
    onSuccess: (SudokuPuzzle) -> Unit,
    onError: (Exception) -> Unit
) {
    val queue = Volley.newRequestQueue(context)
    val url = "https://sudoku-api.vercel.app/api/dosuku" // senza difficulty, la prende API

    val request = JsonObjectRequest(Request.Method.GET, url, null,
        { response ->
            try {
                // Leggi la difficoltà dall'API
                val difficultyStr = response.getString("difficulty").uppercase()
                val difficulty = Difficulty.valueOf(difficultyStr)

                val puzzleArray = response
                    .getJSONObject("newboard")
                    .getJSONArray("grids")
                    .getJSONObject(0)
                    .getJSONArray("value")

                val graph = LinkedHashMap<Int, LinkedList<SudokuNode>>()

                for (row in 0 until puzzleArray.length()) {
                    val rowArray = puzzleArray.getJSONArray(row)
                    val rowList = LinkedList<SudokuNode>()

                    for (col in 0 until rowArray.length()) {
                        val value = rowArray.getInt(col)
                        rowList.add(SudokuNode(row, col, value, value != 0))
                    }

                    graph[row] = rowList
                }

                val puzzle = SudokuPuzzle(1, difficulty, graph)
                onSuccess(puzzle)

            } catch (e: Exception) {
                onError(e)
            }
        },
        { error ->
            onError(error)
        })

    queue.add(request)
}
