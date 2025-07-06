package com.sudokuMaster.ui

import kotlinx.serialization.Serializable

sealed class Screen(val route: String) {

    @Serializable
    object HomeScreen : Screen("home_screen")

    @Serializable
    object StatisticsScreen : Screen("statistics_screen")

    @Serializable
    object ActiveGameScreen : Screen("active_game_screen/{initialGameType}") {
        fun createRoute(initialGameType: String) = "active_game_screen/$initialGameType"
    }
    @Serializable
    object WinScreen : Screen("win_screen")

    @Serializable
    object UserPreferencesScreen : Screen("user_preferences_screen")
}
