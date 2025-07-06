package com.sudokuMaster.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sudokuMaster.data.model.UserStatistics
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatisticsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStatistics(stats: UserStatistics)

    @Update
    suspend fun updateUserStatistics(stats: UserStatistics)

    @Query("SELECT * FROM user_statistics WHERE id = 1")
    fun getUserStatistics(): Flow<UserStatistics?>
}
