package com.sudokuMaster.`data`.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.sudokuMaster.`data`.dao.GameSessionDao
import com.sudokuMaster.`data`.dao.GameSessionDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _gameSessionDao: Lazy<GameSessionDao> = lazy {
    GameSessionDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "ae2cc2b745b425f1662e64f19d6044cd", "677182bba2595edd95e467ef6d169e08") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `game_sessions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `difficulty` TEXT NOT NULL, `start_time_millis` INTEGER NOT NULL, `end_time_millis` INTEGER, `duration_seconds` INTEGER, `points_scored` INTEGER NOT NULL, `is_solved` INTEGER NOT NULL, `initial_grid` TEXT NOT NULL, `current_grid` TEXT NOT NULL, `date_played_millis` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ae2cc2b745b425f1662e64f19d6044cd')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `game_sessions`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsGameSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGameSessions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGameSessions.put("difficulty", TableInfo.Column("difficulty", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGameSessions.put("start_time_millis", TableInfo.Column("start_time_millis",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameSessions.put("end_time_millis", TableInfo.Column("end_time_millis", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameSessions.put("duration_seconds", TableInfo.Column("duration_seconds", "INTEGER",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameSessions.put("points_scored", TableInfo.Column("points_scored", "INTEGER", true,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameSessions.put("is_solved", TableInfo.Column("is_solved", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameSessions.put("initial_grid", TableInfo.Column("initial_grid", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameSessions.put("current_grid", TableInfo.Column("current_grid", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsGameSessions.put("date_played_millis", TableInfo.Column("date_played_millis",
            "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGameSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGameSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoGameSessions: TableInfo = TableInfo("game_sessions", _columnsGameSessions,
            _foreignKeysGameSessions, _indicesGameSessions)
        val _existingGameSessions: TableInfo = read(connection, "game_sessions")
        if (!_infoGameSessions.equals(_existingGameSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |game_sessions(com.sudokuMaster.data.model.GameSession).
              | Expected:
              |""".trimMargin() + _infoGameSessions + """
              |
              | Found:
              |""".trimMargin() + _existingGameSessions)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "game_sessions")
  }

  public override fun clearAllTables() {
    super.performClear(false, "game_sessions")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(GameSessionDao::class, GameSessionDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun gameSessionDao(): GameSessionDao = _gameSessionDao.value
}
