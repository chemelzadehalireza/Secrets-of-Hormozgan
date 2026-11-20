package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "county_progress")
data class CountyProgressEntity(
    @PrimaryKey val countyId: String,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val completedMissionIdsCsv: String,
    val treasureCollected: Boolean,
    val npcMet: Boolean,
    val lastVisitedAt: Long,
)

@Entity(tableName = "player_save")
data class PlayerSaveEntity(
    @PrimaryKey val id: Int = 1,
    val activeCountyId: String,
    val lastPlayerX: Float,
    val hasSeenIntro: Boolean,
    val encyclopediaUnlocked: Boolean,
)

@Dao
interface GameDao {
    @Query("SELECT * FROM county_progress")
    fun observeAllCountyProgress(): Flow<List<CountyProgressEntity>>

    @Query("SELECT * FROM county_progress")
    suspend fun getAllCountyProgressOnce(): List<CountyProgressEntity>

    @Query("SELECT * FROM county_progress WHERE countyId = :countyId LIMIT 1")
    suspend fun getCountyProgressOnce(countyId: String): CountyProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCountyProgress(entity: CountyProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAllCountyProgress(entities: List<CountyProgressEntity>)

    @Query("SELECT * FROM player_save WHERE id = 1 LIMIT 1")
    fun observePlayerSave(): Flow<PlayerSaveEntity?>

    @Query("SELECT * FROM player_save WHERE id = 1 LIMIT 1")
    suspend fun getPlayerSaveOnce(): PlayerSaveEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayerSave(save: PlayerSaveEntity)

    @Query("DELETE FROM county_progress")
    suspend fun clearAllCountyProgress()

    @Query("DELETE FROM player_save")
    suspend fun clearPlayerSave()
}

@Database(
    entities = [CountyProgressEntity::class, PlayerSaveEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class HormozganDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: HormozganDatabase? = null

        fun getInstance(context: Context): HormozganDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HormozganDatabase::class.java,
                    "hormozgan_secrets_db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
