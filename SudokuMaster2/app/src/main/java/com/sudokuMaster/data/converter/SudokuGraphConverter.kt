package com.sudokuMaster.data.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sudokuMaster.domain.SudokuNode
import java.util.LinkedList

/**
 * Type Converter per Room per serializzare e deserializzare il grafo del Sudoku.
 * Converte LinkedHashMap<Int, LinkedList<SudokuNode>> da/a String (JSON).
 */
class SudokuGraphConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromGraph(graph: LinkedHashMap<Int, LinkedList<SudokuNode>>): String {
        return gson.toJson(graph)
    }

    @TypeConverter
    fun toGraph(graphString: String): LinkedHashMap<Int, LinkedList<SudokuNode>> {
        val type = object : TypeToken<LinkedHashMap<Int, LinkedList<SudokuNode>>>() {}.type
        return gson.fromJson(graphString, type)
    }
}
