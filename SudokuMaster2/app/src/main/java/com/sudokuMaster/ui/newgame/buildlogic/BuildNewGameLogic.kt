package com.sudokuMaster.ui.newgame.buildlogic

import android.content.Context
import androidx.room.Room
import com.sudokuMaster.common.ProductionDispatcherProvider
import com.sudokuMaster.data.DifficultyLevel
import com.sudokuMaster.data.database.AppDatabase // Assicurati che AppDatabase esista e sia configurato
import com.sudokuMaster.data.dao.GameSessionDao
import com.sudokuMaster.data.repository.GameRepositoryImpl
import com.sudokuMaster.data.repository.UserPreferencesRepositoryImpl
import com.sudokuMaster.data.source.SudokuApiService
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.data.userPreferencesDataStore // Il tuo DataStore per le preferenze
import com.sudokuMaster.ui.newgame.NewGameViewModel // La nuova ViewModel
import com.sudokuMaster.domain.GameRepositoryInterface // L'interfaccia GameRepository
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface // L'interfaccia UserPreferencesRepository

// Funzione per costruire la ViewModel
internal fun buildNewGameViewModel(context: Context): NewGameViewModel {

    // Inizializza il GameSessionDao da Room
    val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "sudoku_master_db"
    ).build()
    val gameSessionDao: GameSessionDao = db.gameSessionDao() // Assumendo che AppDatabase abbia questo metodo

    // Inizializza UserPreferencesRepositoryImpl con il DataStore
    val userPreferencesRepo: UserPreferencesRepositoryInterface =
        UserPreferencesRepositoryImpl(context.userPreferencesDataStore)

    val sudokuApiService = SudokuApiService.create() // Assicurati che questo metodo sia disponibile
    val sudokuRemoteDataSource = SudokuRemoteDataSource(sudokuApiService)

    // Inizializza GameRepositoryImpl con le dipendenze corrette
    val gameRepository: GameRepositoryInterface =
        GameRepositoryImpl(gameSessionDao, userPreferencesRepo, sudokuRemoteDataSource)

    // Restituisci la NewGameViewModel
    return NewGameViewModel(
        gameRepository = gameRepository,
        userPreferencesRepository = userPreferencesRepo
    )
}
