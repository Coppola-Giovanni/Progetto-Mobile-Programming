
package com.sudokuMaster.logic

import com.sudokuMaster.domain.Difficulty
import com.sudokuMaster.domain.SudokuNode
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.LinkedHashMap
import java.util.LinkedList

data class SudokuGraph(
    val graph: LinkedHashMap<Int, LinkedList<SudokuNode>>
)

fun buildNewSudoku(difficulty: Difficulty): SudokuGraph {
    val client = OkHttpClient()
    val url = "https://sudoku-api.vercel.app/api/dosuku?query=$difficulty"

    val request = Request.Builder()
        .url(url)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw Exception("Unexpected response: $response")

        val body = response.body?.string() ?: throw Exception("Empty response body")
        val json = JSONObject(body)
        val puzzle = json.getJSONArray("newboard")

        val graph = LinkedHashMap<Int, LinkedList<SudokuNode>>()

        for (y in 0 until puzzle.length()) {
            val rowArray = puzzle.getJSONArray(y)
            val rowList = LinkedList<SudokuNode>()
            for (x in 0 until rowArray.length()) {
                val value = rowArray.getInt(x)

                rowList.add(
                    SudokuNode(
                        x = x,
                        y = y,
                        color = value,
                        readOnly = value != 0
                    )
                )
            }
            graph[y] = rowList
        }

        return SudokuGraph(graph)
    }
}