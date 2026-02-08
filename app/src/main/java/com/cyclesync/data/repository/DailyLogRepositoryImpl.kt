package com.cyclesync.data.repository

import com.cyclesync.core.database.dao.DailyLogDao
import com.cyclesync.core.database.dao.TrackingEntryDao
import com.cyclesync.core.database.entity.DailyLogEntity
import com.cyclesync.core.database.entity.TrackingEntryEntity
import com.cyclesync.core.utils.DateUtils
import com.cyclesync.domain.entity.DailyLog
import com.cyclesync.domain.entity.TrackingCategory
import com.cyclesync.domain.entity.TrackingEntry
import com.cyclesync.domain.repository.DailyLogRepository
import com.cyclesync.core.utils.enumValueOfOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyLogRepositoryImpl @Inject constructor(
    private val dailyLogDao: DailyLogDao,
    private val trackingEntryDao: TrackingEntryDao
) : DailyLogRepository {

    override suspend fun getByDate(date: LocalDate): DailyLog? {
        val entity = dailyLogDao.getByDate(DateUtils.toIsoString(date)) ?: return null
        val entries = trackingEntryDao.getByLogId(entity.id).mapNotNull { it.toDomainOrNull() }
        return entity.toDomain(entries)
    }

    override fun getByDateFlow(date: LocalDate): Flow<DailyLog?> {
        return dailyLogDao.getByDateFlow(DateUtils.toIsoString(date)).map { entity ->
            entity?.let {
                val entries = trackingEntryDao.getByLogId(it.id).mapNotNull { e -> e.toDomainOrNull() }
                it.toDomain(entries)
            }
        }
    }

    override suspend fun getByCycleId(cycleId: String): List<DailyLog> {
        return dailyLogDao.getByCycleId(cycleId).map { entity ->
            val entries = trackingEntryDao.getByLogId(entity.id).mapNotNull { it.toDomainOrNull() }
            entity.toDomain(entries)
        }
    }

    override suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<DailyLog> {
        return dailyLogDao.getByDateRange(
            DateUtils.toIsoString(startDate),
            DateUtils.toIsoString(endDate)
        ).map { entity ->
            val entries = trackingEntryDao.getByLogId(entity.id).mapNotNull { it.toDomainOrNull() }
            entity.toDomain(entries)
        }
    }

    override fun getByDateRangeFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyLog>> {
        return dailyLogDao.getByDateRangeFlow(
            DateUtils.toIsoString(startDate),
            DateUtils.toIsoString(endDate)
        ).map { entities ->
            entities.map { entity ->
                val entries = trackingEntryDao.getByLogId(entity.id).mapNotNull { it.toDomainOrNull() }
                entity.toDomain(entries)
            }
        }
    }

    override suspend fun insertOrUpdate(log: DailyLog) {
        val now = DateUtils.toIsoString(LocalDate.now())
        dailyLogDao.insert(
            DailyLogEntity(
                id = log.id,
                date = DateUtils.toIsoString(log.date),
                cycleId = log.cycleId,
                cycleDay = log.cycleDay,
                notes = log.notes?.take(5000),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    override suspend fun delete(log: DailyLog) {
        dailyLogDao.delete(
            DailyLogEntity(
                id = log.id,
                date = DateUtils.toIsoString(log.date),
                cycleId = log.cycleId,
                cycleDay = log.cycleDay,
                notes = log.notes
            )
        )
    }

    override suspend fun saveTrackingEntries(logId: String, entries: List<TrackingEntry>) {
        trackingEntryDao.deleteByLogId(logId)
        if (entries.isNotEmpty()) {
            trackingEntryDao.insertAll(entries.map { it.toEntity() })
        }
    }

    override suspend fun getTrackingEntries(logId: String): List<TrackingEntry> {
        return trackingEntryDao.getByLogId(logId).mapNotNull { it.toDomainOrNull() }
    }

    private fun DailyLogEntity.toDomain(entries: List<TrackingEntry> = emptyList()): DailyLog = DailyLog(
        id = id,
        date = DateUtils.fromIsoString(date),
        cycleId = cycleId,
        cycleDay = cycleDay,
        notes = notes,
        entries = entries
    )

    private fun TrackingEntryEntity.toDomainOrNull(): TrackingEntry? {
        val parsedCategory = enumValueOfOrNull<TrackingCategory>(category) ?: return null
        return TrackingEntry(
            id = id,
            dailyLogId = dailyLogId,
            category = parsedCategory,
            subcategory = subcategory,
            intensity = intensity?.coerceIn(0, 10),
            customValue = customValue
        )
    }

    private fun TrackingEntry.toEntity(): TrackingEntryEntity = TrackingEntryEntity(
        id = id,
        dailyLogId = dailyLogId,
        category = category.name.lowercase(),
        subcategory = subcategory,
        intensity = intensity,
        customValue = customValue,
        createdAt = DateUtils.toIsoString(LocalDate.now())
    )
}
