package com.sudokuMaster.domain

enum class Difficulty(val modifier: Double, val apiParam: String) {
    EASY(0.50, "easy"),
    MEDIUM(0.44, "medium"),
    HARD(0.38, "hard");
}