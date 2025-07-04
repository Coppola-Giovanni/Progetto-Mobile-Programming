package com.sudokuMaster.domain

interface GameDataStorageInterface {                                                            //interfaccia per il filesystem quindi da adattare al database
    suspend fun updateGame(game:SudokuPuzzle): GameStorageResult
    suspend fun updateNode(x: Int, y: Int, elapsedTime: Long): GameStorageResult
    suspend fun getCurrentGame(): GameStorageResult
}

sealed class GameStorageResult {
    data class onSuccess(val currentGame: SudokuPuzzle): GameStorageResult()
    data class onError(val exception: Exception): GameStorageResult()
}