package com.cyclesync.data.repository

import com.cyclesync.core.database.dao.CycleDao
import com.cyclesync.core.database.entity.CycleEntity
import com.cyclesync.core.utils.DateUtils
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.SkipReason
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.core.utils.enumValueOfOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepositoryImpl @Inject constructor(
    private val cycleDao: CycleDao
) : CycleRepository {

    override fun getAllCycles(): Flow<List<Cycle>> {
        return cycleDao.getAllCycles().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getAllCyclesOnce(): List<Cycle> {
        return cycleDao.getAllCyclesOnce().map { it.toDomain() }
    }

    override suspend fun getCycleById(id: String): Cycle? {
        return cycleDao.getCycleById(id)?.toDomain()
    }

    override suspend fun getCurrentCycle(): Cycle? {
        return cycleDao.getCurrentCycle()?.toDomain()
    }

    override fun getCurrentCycleFlow(): Flow<Cycle?> {
        return cycleDao.getCurrentCycleFlow().map { it?.toDomain() }
    }

    override suspend fun getLastCycle(): Cycle? {
        return cycleDao.getLastCycle()?.toDomain()
    }

    override suspend fun getValidCycles(): List<Cycle> {
        return cycleDao.getValidCycles().map { it.toDomain() }
    }

    override suspend fun getCycleCount(): Int {
        return cycleDao.getCycleCount()
    }

    override suspend fun insertCycle(cycle: Cycle) {
        cycleDao.insert(cycle.toEntity())
    }

    override suspend fun updateCycle(cycle: Cycle) {
        cycleDao.update(cycle.toEntity())
    }

    override suspend fun deleteCycle(cycle: Cycle) {
        cycleDao.delete(cycle.toEntity())
    }

    override suspend fun deleteAllCycles() {
        cycleDao.deleteAll()
    }

    private fun CycleEntity.toDomain(): Cycle = Cycle(
        id = id,
        cycleNumber = cycleNumber,
        startDate = DateUtils.fromIsoString(startDate),
        endDate = endDate?.let { DateUtils.fromIsoStringOrNull(it) },
        periodStartDate = DateUtils.fromIsoString(periodStartDate),
        periodEndDate = periodEndDate?.let { DateUtils.fromIsoStringOrNull(it) },
        cycleLength = cycleLength,
        periodLength = periodLength,
        isExcluded = isExcluded,
        notes = notes,
        skipReason = enumValueOfOrNull<SkipReason>(skipReason)
    )

    private fun Cycle.toEntity(): CycleEntity {
        val now = DateUtils.toIsoString(LocalDate.now())
        return CycleEntity(
            id = id,
            cycleNumber = cycleNumber,
            startDate = DateUtils.toIsoString(startDate),
            endDate = endDate?.let { DateUtils.toIsoString(it) },
            periodStartDate = DateUtils.toIsoString(periodStartDate),
            periodEndDate = periodEndDate?.let { DateUtils.toIsoString(it) },
            cycleLength = cycleLength,
            periodLength = periodLength,
            isExcluded = isExcluded,
            notes = notes?.take(5000),
            skipReason = skipReason?.name,
            createdAt = now,
            updatedAt = now
        )
    }
}
