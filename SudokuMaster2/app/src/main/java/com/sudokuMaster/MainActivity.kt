package com.sudokuMaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sudokuMaster.common.ProductionDispatcherProvider
import com.sudokuMaster.data.database.AppDatabase
import com.sudokuMaster.data.repository.GameRepositoryImpl
import com.sudokuMaster.data.repository.UserPreferencesRepositoryImpl
import com.sudokuMaster.data.source.SudokuApiService
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.data.userPreferencesDataStore
import com.sudokuMaster.domain.GameRepositoryInterface
import com.sudokuMaster.domain.UserPreferencesRepositoryInterface
import com.sudokuMaster.ui.Screen
import com.sudokuMaster.ui.activegame.ActiveGameScreen
import com.sudokuMaster.ui.activegame.ActiveGameViewModel
import com.sudokuMaster.ui.activegame.ActiveGameViewModelFactory
import com.sudokuMaster.ui.home.HomeScreen
import com.sudokuMaster.ui.home.WinScreen
import com.sudokuMaster.ui.stats.StatisticsScreen
import com.sudokuMaster.ui.stats.StatisticsViewModel
import com.sudokuMaster.ui.theme.GraphSudokuTheme
import com.sudokuMaster.ui.userpreferences.UserPreferencesScreen
import com.sudokuMaster.ui.userpreferences.UserPreferencesViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : ComponentActivity() {

    private lateinit var gameRepository: GameRepositoryInterface
    private lateinit var userPreferencesRepository: UserPreferencesRepositoryInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val userPreferencesDataStore = applicationContext.userPreferencesDataStore
        userPreferencesRepository = UserPreferencesRepositoryImpl(userPreferencesDataStore)

        val db = AppDatabase.getDatabase(applicationContext)
        val gameSessionDao = db.gameSessionDao()
        val userStatisticsDao = db.userStatisticsDao()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://sudoku-api.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val sudokuApiService = retrofit.create(SudokuApiService::class.java)
        val sudokuRemoteDataSource = SudokuRemoteDataSource(sudokuApiService)

        gameRepository = GameRepositoryImpl(
            gameSessionDao = gameSessionDao,
            userStatisticsDao = userStatisticsDao,
            userPreferencesRepository = userPreferencesRepository,
            sudokuRemoteDataSource = sudokuRemoteDataSource
        )

        setContent {
            GraphSudokuTheme(userPreferencesRepository = userPreferencesRepository) { // <-- MODIFICATO QUI
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
    navController: NavHostController,
    gameRepository: GameRepositoryInterface,
    userPreferencesRepository: UserPreferencesRepositoryInterface
) {
    NavHost( navController = navController,
        startDestination = Screen.HomeScreen.route as String) {
        composable(Screen.HomeScreen.route) {
            HomeScreen(
                onNewGameClick = { navController.navigate(Screen.ActiveGameScreen.createRoute("new")) },
                onContinueGameClick = { navController.navigate(Screen.ActiveGameScreen.createRoute("continue")) },
                onViewStatisticsClick = { navController.navigate(Screen.StatisticsScreen.route) }
            )
        }
        composable(
            route = Screen.ActiveGameScreen.route,
            arguments = listOf(navArgument("initialGameType") { type = NavType.StringType })
        ) { backStackEntry ->
            val initialGameType = backStackEntry.arguments?.getString("initialGameType") ?: "new"

            val activeGameViewModel: ActiveGameViewModel = viewModel(
                factory = ActiveGameViewModelFactory(
                    gameRepository = gameRepository,
                    userPreferencesRepository = userPreferencesRepository,
                    initialGameType = initialGameType
                )
            )

            ActiveGameScreen(
                activeGameViewModel = activeGameViewModel,
                navController = navController
            )
        }
        composable(Screen.StatisticsScreen.route) {
            val statisticsViewModel: StatisticsViewModel = viewModel(
                factory = StatisticsViewModel.Factory(
                    gameRepository = gameRepository,
                    dispatcherProvider = ProductionDispatcherProvider
                )
            )
            StatisticsScreen(
                navController = navController,
                statisticsViewModel = statisticsViewModel
            )
        }
        composable(Screen.WinScreen.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.ActiveGameScreen.route)
            }
            val activeGameViewModel: ActiveGameViewModel = viewModel(
                viewModelStoreOwner = parentEntry,
                factory = ActiveGameViewModelFactory(gameRepository, userPreferencesRepository, "continue")
            )
            WinScreen(
                navController = navController,
                activeGameViewModel = activeGameViewModel
            )
        }
        composable(Screen.UserPreferencesScreen.route){
            val userPreferencesViewModel: UserPreferencesViewModel = viewModel(
                factory = UserPreferencesViewModel.UserPreferencesViewModelFactory(
                    userPreferencesRepository
                )
            )
            UserPreferencesScreen(
                navController = navController,
                userPreferencesViewModel = userPreferencesViewModel
            )
        }

    }
}
