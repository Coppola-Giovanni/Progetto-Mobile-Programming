package com.sudokuMaster.ui.activegame

sealed class ActiveGameEvent {
    data class OnInput(val input: Int) : ActiveGameEvent()
    data class OnTileFocused(val x: Int, val y: Int) : ActiveGameEvent()
    data object OnNewGameClicked : ActiveGameEvent()
    data object OnStart : ActiveGameEvent()
    data object OnStop : ActiveGameEvent()
}