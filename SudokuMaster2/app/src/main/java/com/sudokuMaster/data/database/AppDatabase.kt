package com.sudokuMaster.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sudokuMaster.data.converter.SudokuGraphConverter
import com.sudokuMaster.data.dao.GameSessionDao
import com.sudokuMaster.data.model.GameSession

@Database(
    entities = [GameSession::class], // Tutte le entità del tuo database
    version = 1,                     // La versione del database. Incrementala per le migrazioni.
    exportSchema = false             // Imposta a true per esportare lo schema per le migrazioni in produzione.
)
@TypeConverters(SudokuGraphConverter::class) // Registra qui i tuoi TypeConverters
abstract class AppDatabase : RoomDatabase() {

    // Metodi astratti per ottenere le istanze dei DAO
    abstract fun gameSessionDao(): GameSessionDao

    companion object {
        // La singola istanza del database per evitare problemi di concorrenza.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Ottiene l'istanza singleton del database.
         * Se l'istanza non esiste, ne crea una nuova.
         * @param context Il contesto dell'applicazione.
         * @return L'istanza di AppDatabase.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Se l'istanza non è null, restituiscila. Altrimenti, crea un nuovo database.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Usa il contesto dell'applicazione per evitare memory leak
                    AppDatabase::class.java,
                    "sudoku_master_db" // Nome del file del database
                )
                    // Strategia di migrazione distruttiva per lo sviluppo.
                    // In produzione, dovresti gestire le migrazioni in modo più controllato.
                    // .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
