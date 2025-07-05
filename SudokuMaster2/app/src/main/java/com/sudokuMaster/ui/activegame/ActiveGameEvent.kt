package com.sudokuMaster.ui.activegame

sealed class ActiveGameEvent {
    data class onInput(val input: Int) : ActiveGameEvent()
    data class onTileFocused(val x: Int, val y: Int) : ActiveGameEvent()
    object OnNewGameClicked : ActiveGameEvent()
    object OnStart : ActiveGameEvent()
    object OnStop : ActiveGameEvent()
    object OnSuggestMoveClicked : ActiveGameEvent()
}