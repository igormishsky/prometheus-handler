package com.cyclesync.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cyclesync.core.database.entity.TrackingEntryEntity

@Dao
interface TrackingEntryDao {
    @Query("SELECT * FROM tracking_entries WHERE daily_log_id = :logId")
    suspend fun getByLogId(logId: String): List<TrackingEntryEntity>

    @Query("SELECT * FROM tracking_entries WHERE category = :category")
    suspend fun getByCategory(category: String): List<TrackingEntryEntity>

    @Query("SELECT * FROM tracking_entries WHERE daily_log_id = :logId AND category = :category")
    suspend fun getByLogAndCategory(logId: String, category: String): List<TrackingEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TrackingEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TrackingEntryEntity>)

    @Delete
    suspend fun delete(entry: TrackingEntryEntity)

    @Query("DELETE FROM tracking_entries WHERE daily_log_id = :logId")
    suspend fun deleteByLogId(logId: String)

    @Query("DELETE FROM tracking_entries WHERE daily_log_id = :logId AND category = :category")
    suspend fun deleteByLogAndCategory(logId: String, category: String)
}
