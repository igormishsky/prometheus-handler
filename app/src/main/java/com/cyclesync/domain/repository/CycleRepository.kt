package com.cyclesync.domain.repository

import com.cyclesync.domain.entity.Cycle
import kotlinx.coroutines.flow.Flow

interface CycleRepository {
    fun getAllCycles(): Flow<List<Cycle>>
    suspend fun getAllCyclesOnce(): List<Cycle>
    suspend fun getCycleById(id: String): Cycle?
    suspend fun getCurrentCycle(): Cycle?
    fun getCurrentCycleFlow(): Flow<Cycle?>
    suspend fun getLastCycle(): Cycle?
    suspend fun getValidCycles(): List<Cycle>
    suspend fun getCycleCount(): Int
    suspend fun insertCycle(cycle: Cycle)
    suspend fun updateCycle(cycle: Cycle)
    suspend fun deleteCycle(cycle: Cycle)
    suspend fun deleteAllCycles()
}
