package com.cyclesync.domain.repository

import com.cyclesync.domain.entity.DailyLog
import com.cyclesync.domain.entity.TrackingEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyLogRepository {
    suspend fun getByDate(date: LocalDate): DailyLog?
    fun getByDateFlow(date: LocalDate): Flow<DailyLog?>
    suspend fun getByCycleId(cycleId: String): List<DailyLog>
    suspend fun getByDateRange(startDate: LocalDate, endDate: LocalDate): List<DailyLog>
    fun getByDateRangeFlow(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyLog>>
    suspend fun insertOrUpdate(log: DailyLog)
    suspend fun delete(log: DailyLog)
    suspend fun saveTrackingEntries(logId: String, entries: List<TrackingEntry>)
    suspend fun getTrackingEntries(logId: String): List<TrackingEntry>
}
