package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_scripts")
data class SavedScript(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "SCRIPT" or "IDEAS"
    val topic: String,
    val niche: String,
    val language: String,
    val hook: String = "",
    val body: String = "",
    val cta: String = "",
    val visuals: String = "",
    val hashtags: String = "",
    val ideasJson: String = "", // Used to store multiple ideas in a list if type is "IDEAS"
    val timelineJson: String = "", // JSON representation of script timeline items
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ScriptDao {
    @Query("SELECT * FROM saved_scripts ORDER BY timestamp DESC")
    fun getAllSavedScripts(): Flow<List<SavedScript>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScript(script: SavedScript): Long

    @Query("DELETE FROM saved_scripts WHERE id = :id")
    suspend fun deleteScriptById(id: Int)
}

@Database(entities = [SavedScript::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scriptDao(): ScriptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "viral_script_writer_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
