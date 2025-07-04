package com.sudokuMaster.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "game_sessions")
data class GameSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val difficulty: String,

    @ColumnInfo(name = "initial_grid")
    val initialGrid: String,

    @ColumnInfo(name = "current_grid")
    val currentGrid: String,

    @ColumnInfo(name = "start_time_millis")
    val startTimeMillis: Long,

    @ColumnInfo(name = "end_time_millis")
    val endTimeMillis: Long?,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long?,

    @ColumnInfo(name = "points_scored")
    val score: Int,

    @ColumnInfo(name = "is_solved")
    val isSolved: Boolean,

    @ColumnInfo(name = "date_played_millis")
    val datePlayedMillis: Long
)
