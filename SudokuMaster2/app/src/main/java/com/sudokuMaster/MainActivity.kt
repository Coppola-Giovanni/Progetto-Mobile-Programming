package com.sudokuMaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.sudokuMaster.data.database.AppDatabase
import com.sudokuMaster.data.repository.GameRepositoryImpl
import com.sudokuMaster.data.repository.UserPreferencesRepositoryImpl
import com.sudokuMaster.data.source.SudokuApiService
import com.sudokuMaster.data.source.SudokuRemoteDataSource
import com.sudokuMaster.ui.theme.SudokuMasterTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {

    private val appDatabase by lazy {
        AppDatabase.getDatabase(applicationContext)
    }

    private val userPreferencesDataStore by lazy {
        DataStoreFactory.create(
            serializer = UserPreferencesSerializer,
            produceFile = { applicationContext.dataStoreFile("user_preferences.pb") }
        )
    }

    private val userPreferencesRepository by lazy {
        UserPreferencesRepositoryImpl(userPreferencesDataStore)
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://sudoku-api.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val sudokuApiService by lazy {
        retrofit.create(SudokuApiService::class.java)
    }

    private val sudokuRemoteDataSource by lazy {
        SudokuRemoteDataSource(sudokuApiService)
    }

    private val gameRepository by lazy {
        GameRepositoryImpl(
            appDatabase.gameSessionDao(),
            userPreferencesRepository,
            sudokuRemoteDataSource
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SudokuMasterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SudokuMasterTheme {
        Greeting("Android")
    }
}