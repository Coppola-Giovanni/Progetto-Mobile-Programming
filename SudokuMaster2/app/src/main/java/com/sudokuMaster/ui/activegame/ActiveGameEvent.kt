package com.sudokuMaster.ui.activegame

sealed class ActiveGameEvent {
    data class OnInput(val input: Int) : ActiveGameEvent()
    data class OnNoteInput(val input: Int) : ActiveGameEvent()
    data class OnTileFocused(val x: Int, val y: Int) : ActiveGameEvent()
    object OnNewGameClicked : ActiveGameEvent()
    object OnStart : ActiveGameEvent()
    object OnStop : ActiveGameEvent()
    object OnSuggestMoveClicked : ActiveGameEvent()
    object OnToggleNotesMode : ActiveGameEvent()
}