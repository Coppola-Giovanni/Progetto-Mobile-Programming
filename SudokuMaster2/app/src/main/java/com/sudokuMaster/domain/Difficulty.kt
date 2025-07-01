package com.sudokuMaster.domain

enum class Difficulty(val modifier: Double) {
    val apiParam: String

    EASY(0.50),
    MEDIUM(0.44),
    HARD(0.38);
}