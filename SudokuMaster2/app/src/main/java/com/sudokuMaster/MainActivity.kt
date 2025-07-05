package com.sudokuMaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sudokuMaster.data.UserPreferencesSerializer
import com.sudokuMaster.data.database.AppDatabase
import com.sudokuMaster.data.repository.GameRepositoryImpl
import com.sudokuMaster.data.repository.UserPreferencesRepositoryImpl
import com.sudokuMaster.data.source.SudokuApiService
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.data.UserPreferences
import com.sudokuMaster.data.userPreferencesDataStore
import com.sudokuMaster.ui.activegame.ActiveGameScreen // Importa ActiveGameScreen
import com.sudokuMaster.ui.activegame.ActiveGameViewModelFactory // Importa ActiveGameViewModelFactory
import com.sudokuMaster.ui.home.HomeScreen // Importa HomeScreen
import com.sudokuMaster.ui.stats.StatisticsScreen // Importa StatisticsScreen
import com.sudokuMaster.ui.theme.GraphSudokuTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
// Non è necessario importare ProductionDispatcherProvider qui a meno che non lo usi direttamente
// import com.sudokuMaster.common.ProductionDispatcherProvider

class MainActivity : ComponentActivity() {

    // Dichiarare le dipendenze come lateinit var a livello di classe
    // per poterle inizializzare una volta e passarle ai Composable.
    private lateinit var gameRepository: GameRepositoryImpl
    private lateinit var userPreferencesRepository: UserPreferencesRepositoryImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- INIEZIONE DELLE DIPENDENZE ---
        // 1. DataStore per UserPreferences
        val userPreferencesDataStore = applicationContext.userPreferencesDataStore
        userPreferencesRepository = UserPreferencesRepositoryImpl(userPreferencesDataStore)

        // 2. Room Database
        val db = AppDatabase.getDatabase(applicationContext)
        val gameSessionDao = db.gameSessionDao()

        // 3. Retrofit per API esterna
        val retrofit = Retrofit.Builder()
            .baseUrl("https://sudoku-api.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val sudokuApiService = retrofit.create(SudokuApiService::class.java)
        val sudokuRemoteDataSource = SudokuRemoteDataSource(sudokuApiService)

        // 4. GameRepository
        gameRepository = GameRepositoryImpl(
            gameSessionDao = gameSessionDao,
            userPreferencesRepository = userPreferencesRepository,
            sudokuRemoteDataSource = sudokuRemoteDataSource
        )
        // --- FINE INIEZIONE DIPENDENZE ---

        setContent {
            GraphSudokuTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    SudokuAppNavigation(
                        navController = navController,
                        gameRepository = gameRepository,
                        userPreferencesRepository = userPreferencesRepository
                    )
                }
            }
        }
    }
}

@Composable
fun SudokuAppNavigation(
    navController: NavController,
    gameRepository: GameRepositoryImpl,
    userPreferencesRepository: UserPreferencesRepositoryImpl
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home_screen") {
        composable("home_screen") {
            HomeScreen(
                onNewGameClick = { navController.navigate("active_game_screen/new") },
                onContinueGameClick = { navController.navigate("active_game_screen/continue") },
                onViewStatisticsClick = { navController.navigate("statistics_screen") }
            )
        }
        composable("active_game_screen/{gameType}") { backStackEntry ->
            val gameType = backStackEntry.arguments?.getString("gameType") ?: "new" // Default a "new"

            val activeGameViewModelFactory = ActiveGameViewModelFactory(
                gameRepository = gameRepository,
                userPreferencesRepository = userPreferencesRepository,
                initialGameType = gameType // Passa il gameType dal navigazione
            )

            ActiveGameScreen(
                viewModelFactory = activeGameViewModelFactory,
                onGameSolved = { navController.popBackStack() }, // Torna alla home o gestisci il popup di vittoria
                onGameLoadError = { navController.popBackStack() } // Torna alla home in caso di errore di caricamento
            )
        }
        composable("statistics_screen") {
            StatisticsScreen(onBackClick = { navController.popBackStack() })
        }
    }
}