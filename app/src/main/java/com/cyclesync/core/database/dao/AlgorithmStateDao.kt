package com.cyclesync.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cyclesync.core.database.entity.AlgorithmStateEntity

@Dao
interface AlgorithmStateDao {
    @Query("SELECT * FROM algorithm_state WHERE id = 1")
    suspend fun getState(): AlgorithmStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(state: AlgorithmStateEntity)

    @Update
    suspend fun update(state: AlgorithmStateEntity)
}
