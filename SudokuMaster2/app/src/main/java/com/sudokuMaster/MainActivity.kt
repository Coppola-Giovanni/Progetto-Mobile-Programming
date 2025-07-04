package com.sudokuMaster

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import com.sudokuMaster.data.UserPreferencesSerializer
import com.sudokuMaster.data.database.AppDatabase
import com.sudokuMaster.data.repository.GameRepositoryImpl
import com.sudokuMaster.data.repository.UserPreferencesRepositoryImpl
import com.sudokuMaster.data.source.SudokuApiService
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.data.UserPreferences
import com.sudokuMaster.data.userPreferencesDataStore
import com.sudokuMaster.ui.activegame.ActiveGameScreen
import com.sudokuMaster.ui.activegame.ActiveGameViewModelFactory
import com.sudokuMaster.ui.theme.GraphSudokuTheme
import retrofit2.Retrofit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import retrofit2.converter.gson.GsonConverterFactory



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- INIEZIONE DELLE DIPENDENZE (MANUALE) ---
        // 1. DataStore
        val userDataStore: DataStore<UserPreferences> = applicationContext.userPreferencesDataStore

        // 2. Room Database
        val db = AppDatabase.getDatabase(applicationContext)
        val gameSessionDao = db.gameSessionDao()

        // 3. Retrofit e SudokuApiService (per chiamate API)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://sudoku-api.vercel.app/") // L'URL base della tua API
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(SudokuApiService::class.java)

        // 4. Remote Data Source
        val sudokuRemoteDataSource = SudokuRemoteDataSource(apiService)

        // 5. UserPreferencesRepository
        val userPreferencesRepository = UserPreferencesRepositoryImpl(userPreferencesDataStore)

        // 6. GameRepository
        val gameRepository = GameRepositoryImpl(
            gameSessionDao = gameSessionDao,
            userPreferencesRepository = userPreferencesRepository,
            sudokuRemoteDataSource = sudokuRemoteDataSource
        )

        // 7. ViewModel Factory
        val activeGameViewModelFactory = ActiveGameViewModelFactory(
            gameRepository = gameRepository,
            userPreferencesRepository = userPreferencesRepository
        )
        // --- FINE INIEZIONE DIPENDENZE ---

        setContent {
            GraphSudokuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "activeGame") {
                        composable("activeGame") {
                            ActiveGameScreen(
                                viewModelFactory = activeGameViewModelFactory
                            )
                        }
                        // In futuro, potresti avere altre destinazioni come:
                        // composable("homeScreen") { HomeScreen(navController) }
                        // composable("settingsScreen") { SettingsScreen(navController) }
                    }
                }
            }
        }
    }
}
