package com.sudokuMaster.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sudokuMaster.data.converter.SudokuGraphConverter
import com.sudokuMaster.data.dao.GameSessionDao
import com.sudokuMaster.data.dao.UserStatisticsDAO
import com.sudokuMaster.data.model.GameSession
import com.sudokuMaster.data.model.UserStatistics

@Database(
    entities = [GameSession::class, UserStatistics::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(SudokuGraphConverter::class)
abstract class AppDatabase : RoomDatabase() {

    // Metodi astratti per ottenere le istanze dei DAO
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun userStatisticsDao(): UserStatisticsDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Ottiene l'istanza singleton del database.
         * Se l'istanza non esiste, ne crea una nuova.
         * @param context Il contesto dell'applicazione.
         * @return L'istanza di AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sudoku_master_db" // Nome del file del database
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
