package com.cyclesync.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyclesync.core.database.entity.CycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycles ORDER BY start_date DESC")
    fun getAllCycles(): Flow<List<CycleEntity>>

    @Query("SELECT * FROM cycles ORDER BY start_date DESC")
    suspend fun getAllCyclesOnce(): List<CycleEntity>

    @Query("SELECT * FROM cycles WHERE id = :id")
    suspend fun getCycleById(id: String): CycleEntity?

    @Query("SELECT * FROM cycles WHERE end_date IS NULL ORDER BY start_date DESC LIMIT 1")
    suspend fun getCurrentCycle(): CycleEntity?

    @Query("SELECT * FROM cycles WHERE end_date IS NULL ORDER BY start_date DESC LIMIT 1")
    fun getCurrentCycleFlow(): Flow<CycleEntity?>

    @Query("SELECT * FROM cycles ORDER BY start_date DESC LIMIT 1")
    suspend fun getLastCycle(): CycleEntity?

    @Query("SELECT * FROM cycles WHERE is_excluded = 0 ORDER BY start_date DESC")
    suspend fun getValidCycles(): List<CycleEntity>

    @Query("SELECT COUNT(*) FROM cycles")
    suspend fun getCycleCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycle: CycleEntity)

    @Update
    suspend fun update(cycle: CycleEntity)

    @Delete
    suspend fun delete(cycle: CycleEntity)

    @Query("DELETE FROM cycles")
    suspend fun deleteAll()
}
