package com.sudokuMaster.`data`.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.sudokuMaster.`data`.model.GameSession
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class GameSessionDao_Impl(
  __db: RoomDatabase,
) : GameSessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfGameSession: EntityInsertAdapter<GameSession>

  private val __deleteAdapterOfGameSession: EntityDeleteOrUpdateAdapter<GameSession>

  private val __updateAdapterOfGameSession: EntityDeleteOrUpdateAdapter<GameSession>
  init {
    this.__db = __db
    this.__insertAdapterOfGameSession = object : EntityInsertAdapter<GameSession>() {
      protected override fun createQuery(): String =
          "INSERT OR ABORT INTO `game_sessions` (`id`,`difficulty`,`initial_grid`,`current_grid`,`start_time_millis`,`end_time_millis`,`duration_seconds`,`points_scored`,`is_solved`,`date_played_millis`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GameSession) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.difficulty)
        statement.bindText(3, entity.initialGrid)
        statement.bindText(4, entity.currentGrid)
        statement.bindLong(5, entity.startTimeMillis)
        val _tmpEndTimeMillis: Long? = entity.endTimeMillis
        if (_tmpEndTimeMillis == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpEndTimeMillis)
        }
        val _tmpDurationSeconds: Long? = entity.durationSeconds
        if (_tmpDurationSeconds == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpDurationSeconds)
        }
        statement.bindLong(8, entity.score.toLong())
        val _tmp: Int = if (entity.isSolved) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        statement.bindLong(10, entity.datePlayedMillis)
      }
    }
    this.__deleteAdapterOfGameSession = object : EntityDeleteOrUpdateAdapter<GameSession>() {
      protected override fun createQuery(): String = "DELETE FROM `game_sessions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GameSession) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfGameSession = object : EntityDeleteOrUpdateAdapter<GameSession>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `game_sessions` SET `id` = ?,`difficulty` = ?,`initial_grid` = ?,`current_grid` = ?,`start_time_millis` = ?,`end_time_millis` = ?,`duration_seconds` = ?,`points_scored` = ?,`is_solved` = ?,`date_played_millis` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: GameSession) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.difficulty)
        statement.bindText(3, entity.initialGrid)
        statement.bindText(4, entity.currentGrid)
        statement.bindLong(5, entity.startTimeMillis)
        val _tmpEndTimeMillis: Long? = entity.endTimeMillis
        if (_tmpEndTimeMillis == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpEndTimeMillis)
        }
        val _tmpDurationSeconds: Long? = entity.durationSeconds
        if (_tmpDurationSeconds == null) {
          statement.bindNull(7)
        } else {
          statement.bindLong(7, _tmpDurationSeconds)
        }
        statement.bindLong(8, entity.score.toLong())
        val _tmp: Int = if (entity.isSolved) 1 else 0
        statement.bindLong(9, _tmp.toLong())
        statement.bindLong(10, entity.datePlayedMillis)
        statement.bindLong(11, entity.id)
      }
    }
  }

  public override suspend fun insertGameSession(gameSession: GameSession): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfGameSession.insertAndReturnId(_connection, gameSession)
    _result
  }

  public override suspend fun deleteGameSession(gameSession: GameSession): Unit =
      performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfGameSession.handle(_connection, gameSession)
  }

  public override suspend fun updateGameSession(gameSession: GameSession): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfGameSession.handle(_connection, gameSession)
  }

  public override fun getGameSessionById(sessionId: Long): Flow<GameSession?> {
    val _sql: String = "SELECT * FROM game_sessions WHERE id = ?"
    return createFlow(__db, false, arrayOf("game_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfInitialGrid: Int = getColumnIndexOrThrow(_stmt, "initial_grid")
        val _columnIndexOfCurrentGrid: Int = getColumnIndexOrThrow(_stmt, "current_grid")
        val _columnIndexOfStartTimeMillis: Int = getColumnIndexOrThrow(_stmt, "start_time_millis")
        val _columnIndexOfEndTimeMillis: Int = getColumnIndexOrThrow(_stmt, "end_time_millis")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "duration_seconds")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "points_scored")
        val _columnIndexOfIsSolved: Int = getColumnIndexOrThrow(_stmt, "is_solved")
        val _columnIndexOfDatePlayedMillis: Int = getColumnIndexOrThrow(_stmt, "date_played_millis")
        val _result: GameSession?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpInitialGrid: String
          _tmpInitialGrid = _stmt.getText(_columnIndexOfInitialGrid)
          val _tmpCurrentGrid: String
          _tmpCurrentGrid = _stmt.getText(_columnIndexOfCurrentGrid)
          val _tmpStartTimeMillis: Long
          _tmpStartTimeMillis = _stmt.getLong(_columnIndexOfStartTimeMillis)
          val _tmpEndTimeMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndTimeMillis)) {
            _tmpEndTimeMillis = null
          } else {
            _tmpEndTimeMillis = _stmt.getLong(_columnIndexOfEndTimeMillis)
          }
          val _tmpDurationSeconds: Long?
          if (_stmt.isNull(_columnIndexOfDurationSeconds)) {
            _tmpDurationSeconds = null
          } else {
            _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          }
          val _tmpScore: Int
          _tmpScore = _stmt.getLong(_columnIndexOfScore).toInt()
          val _tmpIsSolved: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSolved).toInt()
          _tmpIsSolved = _tmp != 0
          val _tmpDatePlayedMillis: Long
          _tmpDatePlayedMillis = _stmt.getLong(_columnIndexOfDatePlayedMillis)
          _result =
              GameSession(_tmpId,_tmpDifficulty,_tmpInitialGrid,_tmpCurrentGrid,_tmpStartTimeMillis,_tmpEndTimeMillis,_tmpDurationSeconds,_tmpScore,_tmpIsSolved,_tmpDatePlayedMillis)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllGameSessions(): Flow<List<GameSession>> {
    val _sql: String = "SELECT * FROM game_sessions ORDER BY date_played_millis DESC"
    return createFlow(__db, false, arrayOf("game_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfInitialGrid: Int = getColumnIndexOrThrow(_stmt, "initial_grid")
        val _columnIndexOfCurrentGrid: Int = getColumnIndexOrThrow(_stmt, "current_grid")
        val _columnIndexOfStartTimeMillis: Int = getColumnIndexOrThrow(_stmt, "start_time_millis")
        val _columnIndexOfEndTimeMillis: Int = getColumnIndexOrThrow(_stmt, "end_time_millis")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "duration_seconds")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "points_scored")
        val _columnIndexOfIsSolved: Int = getColumnIndexOrThrow(_stmt, "is_solved")
        val _columnIndexOfDatePlayedMillis: Int = getColumnIndexOrThrow(_stmt, "date_played_millis")
        val _result: MutableList<GameSession> = mutableListOf()
        while (_stmt.step()) {
          val _item: GameSession
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpInitialGrid: String
          _tmpInitialGrid = _stmt.getText(_columnIndexOfInitialGrid)
          val _tmpCurrentGrid: String
          _tmpCurrentGrid = _stmt.getText(_columnIndexOfCurrentGrid)
          val _tmpStartTimeMillis: Long
          _tmpStartTimeMillis = _stmt.getLong(_columnIndexOfStartTimeMillis)
          val _tmpEndTimeMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndTimeMillis)) {
            _tmpEndTimeMillis = null
          } else {
            _tmpEndTimeMillis = _stmt.getLong(_columnIndexOfEndTimeMillis)
          }
          val _tmpDurationSeconds: Long?
          if (_stmt.isNull(_columnIndexOfDurationSeconds)) {
            _tmpDurationSeconds = null
          } else {
            _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          }
          val _tmpScore: Int
          _tmpScore = _stmt.getLong(_columnIndexOfScore).toInt()
          val _tmpIsSolved: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSolved).toInt()
          _tmpIsSolved = _tmp != 0
          val _tmpDatePlayedMillis: Long
          _tmpDatePlayedMillis = _stmt.getLong(_columnIndexOfDatePlayedMillis)
          _item =
              GameSession(_tmpId,_tmpDifficulty,_tmpInitialGrid,_tmpCurrentGrid,_tmpStartTimeMillis,_tmpEndTimeMillis,_tmpDurationSeconds,_tmpScore,_tmpIsSolved,_tmpDatePlayedMillis)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getLatestUnfinishedGameSession(): Flow<GameSession?> {
    val _sql: String =
        "SELECT * FROM game_sessions WHERE is_solved = 0 ORDER BY start_time_millis DESC LIMIT 1"
    return createFlow(__db, false, arrayOf("game_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfInitialGrid: Int = getColumnIndexOrThrow(_stmt, "initial_grid")
        val _columnIndexOfCurrentGrid: Int = getColumnIndexOrThrow(_stmt, "current_grid")
        val _columnIndexOfStartTimeMillis: Int = getColumnIndexOrThrow(_stmt, "start_time_millis")
        val _columnIndexOfEndTimeMillis: Int = getColumnIndexOrThrow(_stmt, "end_time_millis")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "duration_seconds")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "points_scored")
        val _columnIndexOfIsSolved: Int = getColumnIndexOrThrow(_stmt, "is_solved")
        val _columnIndexOfDatePlayedMillis: Int = getColumnIndexOrThrow(_stmt, "date_played_millis")
        val _result: GameSession?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpInitialGrid: String
          _tmpInitialGrid = _stmt.getText(_columnIndexOfInitialGrid)
          val _tmpCurrentGrid: String
          _tmpCurrentGrid = _stmt.getText(_columnIndexOfCurrentGrid)
          val _tmpStartTimeMillis: Long
          _tmpStartTimeMillis = _stmt.getLong(_columnIndexOfStartTimeMillis)
          val _tmpEndTimeMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndTimeMillis)) {
            _tmpEndTimeMillis = null
          } else {
            _tmpEndTimeMillis = _stmt.getLong(_columnIndexOfEndTimeMillis)
          }
          val _tmpDurationSeconds: Long?
          if (_stmt.isNull(_columnIndexOfDurationSeconds)) {
            _tmpDurationSeconds = null
          } else {
            _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          }
          val _tmpScore: Int
          _tmpScore = _stmt.getLong(_columnIndexOfScore).toInt()
          val _tmpIsSolved: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSolved).toInt()
          _tmpIsSolved = _tmp != 0
          val _tmpDatePlayedMillis: Long
          _tmpDatePlayedMillis = _stmt.getLong(_columnIndexOfDatePlayedMillis)
          _result =
              GameSession(_tmpId,_tmpDifficulty,_tmpInitialGrid,_tmpCurrentGrid,_tmpStartTimeMillis,_tmpEndTimeMillis,_tmpDurationSeconds,_tmpScore,_tmpIsSolved,_tmpDatePlayedMillis)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getSolvedGameSessionsSortedByScore(): Flow<List<GameSession>> {
    val _sql: String = "SELECT * FROM game_sessions WHERE is_solved = 1 ORDER BY points_scored DESC"
    return createFlow(__db, false, arrayOf("game_sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDifficulty: Int = getColumnIndexOrThrow(_stmt, "difficulty")
        val _columnIndexOfInitialGrid: Int = getColumnIndexOrThrow(_stmt, "initial_grid")
        val _columnIndexOfCurrentGrid: Int = getColumnIndexOrThrow(_stmt, "current_grid")
        val _columnIndexOfStartTimeMillis: Int = getColumnIndexOrThrow(_stmt, "start_time_millis")
        val _columnIndexOfEndTimeMillis: Int = getColumnIndexOrThrow(_stmt, "end_time_millis")
        val _columnIndexOfDurationSeconds: Int = getColumnIndexOrThrow(_stmt, "duration_seconds")
        val _columnIndexOfScore: Int = getColumnIndexOrThrow(_stmt, "points_scored")
        val _columnIndexOfIsSolved: Int = getColumnIndexOrThrow(_stmt, "is_solved")
        val _columnIndexOfDatePlayedMillis: Int = getColumnIndexOrThrow(_stmt, "date_played_millis")
        val _result: MutableList<GameSession> = mutableListOf()
        while (_stmt.step()) {
          val _item: GameSession
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDifficulty: String
          _tmpDifficulty = _stmt.getText(_columnIndexOfDifficulty)
          val _tmpInitialGrid: String
          _tmpInitialGrid = _stmt.getText(_columnIndexOfInitialGrid)
          val _tmpCurrentGrid: String
          _tmpCurrentGrid = _stmt.getText(_columnIndexOfCurrentGrid)
          val _tmpStartTimeMillis: Long
          _tmpStartTimeMillis = _stmt.getLong(_columnIndexOfStartTimeMillis)
          val _tmpEndTimeMillis: Long?
          if (_stmt.isNull(_columnIndexOfEndTimeMillis)) {
            _tmpEndTimeMillis = null
          } else {
            _tmpEndTimeMillis = _stmt.getLong(_columnIndexOfEndTimeMillis)
          }
          val _tmpDurationSeconds: Long?
          if (_stmt.isNull(_columnIndexOfDurationSeconds)) {
            _tmpDurationSeconds = null
          } else {
            _tmpDurationSeconds = _stmt.getLong(_columnIndexOfDurationSeconds)
          }
          val _tmpScore: Int
          _tmpScore = _stmt.getLong(_columnIndexOfScore).toInt()
          val _tmpIsSolved: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSolved).toInt()
          _tmpIsSolved = _tmp != 0
          val _tmpDatePlayedMillis: Long
          _tmpDatePlayedMillis = _stmt.getLong(_columnIndexOfDatePlayedMillis)
          _item =
              GameSession(_tmpId,_tmpDifficulty,_tmpInitialGrid,_tmpCurrentGrid,_tmpStartTimeMillis,_tmpEndTimeMillis,_tmpDurationSeconds,_tmpScore,_tmpIsSolved,_tmpDatePlayedMillis)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
