package com.sudokuMaster.ui

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
    object StatisticsScreen : Screen("statistics_screen")
    object ActiveGameScreen : Screen("active_game_screen/{initialGameType}") {
        fun createRoute(initialGameType: String) = "active_game_screen/$initialGameType"
    }
    object WinScreen : Screen("win_screen") // AGGIUNGI QUESTA
}
