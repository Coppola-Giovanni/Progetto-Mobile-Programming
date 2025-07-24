package com.sudokuMaster.domain

data class SudokuNode(
    val x: Int,
    val y: Int,
    var color: Int = 0,
    var readOnly: Boolean = true,
    var notes: Set<Int> = emptySet()
) {
    override fun hashCode(): Int {
        return getHash(x,y)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuNode

        if (x != other.x) return false
        if (y != other.y) return false
        if (color != other.color) return false
        if (readOnly != other.readOnly) return false
        if (notes != other.notes) return false

        return true
    }
}

internal fun getHash(x: Int, y: Int): Int {
    val newX = x*100
    return "$newX$y".toInt()
}