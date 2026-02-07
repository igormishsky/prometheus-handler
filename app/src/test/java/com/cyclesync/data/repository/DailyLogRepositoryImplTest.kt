package com.cyclesync.data.repository

import com.cyclesync.core.database.dao.DailyLogDao
import com.cyclesync.core.database.dao.TrackingEntryDao
import com.cyclesync.core.database.entity.DailyLogEntity
import com.cyclesync.core.database.entity.TrackingEntryEntity
import com.cyclesync.domain.entity.DailyLog
import com.cyclesync.domain.entity.TrackingCategory
import com.cyclesync.domain.entity.TrackingEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class DailyLogRepositoryImplTest {

    private lateinit var dailyLogDao: DailyLogDao
    private lateinit var trackingEntryDao: TrackingEntryDao
    private lateinit var repository: DailyLogRepositoryImpl

    @Before
    fun setUp() {
        dailyLogDao = mockk(relaxed = true)
        trackingEntryDao = mockk(relaxed = true)
        repository = DailyLogRepositoryImpl(dailyLogDao, trackingEntryDao)
    }

    @Test
    fun `getByDate returns null when no log exists`() = runTest {
        coEvery { dailyLogDao.getByDate("2024-01-15") } returns null

        val result = repository.getByDate(LocalDate.of(2024, 1, 15))
        assertNull(result)
    }

    @Test
    fun `getByDate returns log with entries`() = runTest {
        val entity = DailyLogEntity(
            id = "log1", date = "2024-01-15",
            cycleId = "c1", cycleDay = 5, notes = "Test"
        )
        val entries = listOf(
            TrackingEntryEntity(
                id = "e1", dailyLogId = "log1",
                category = "pain", subcategory = "cramps"
            )
        )
        coEvery { dailyLogDao.getByDate("2024-01-15") } returns entity
        coEvery { trackingEntryDao.getByLogId("log1") } returns entries

        val result = repository.getByDate(LocalDate.of(2024, 1, 15))

        assertNotNull(result)
        assertEquals("log1", result!!.id)
        assertEquals(LocalDate.of(2024, 1, 15), result.date)
        assertEquals("c1", result.cycleId)
        assertEquals(5, result.cycleDay)
        assertEquals("Test", result.notes)
        assertEquals(1, result.entries.size)
        assertEquals(TrackingCategory.PAIN, result.entries[0].category)
    }

    @Test
    fun `getByDate filters out invalid tracking categories`() = runTest {
        val entity = DailyLogEntity(id = "log1", date = "2024-01-15")
        val entries = listOf(
            TrackingEntryEntity(id = "e1", dailyLogId = "log1", category = "pain", subcategory = "cramps"),
            TrackingEntryEntity(id = "e2", dailyLogId = "log1", category = "invalid_category", subcategory = "test")
        )
        coEvery { dailyLogDao.getByDate("2024-01-15") } returns entity
        coEvery { trackingEntryDao.getByLogId("log1") } returns entries

        val result = repository.getByDate(LocalDate.of(2024, 1, 15))

        assertNotNull(result)
        assertEquals(1, result!!.entries.size)
        assertEquals(TrackingCategory.PAIN, result.entries[0].category)
    }

    @Test
    fun `getByCycleId returns logs for cycle`() = runTest {
        val entities = listOf(
            DailyLogEntity(id = "log1", date = "2024-01-15", cycleId = "c1"),
            DailyLogEntity(id = "log2", date = "2024-01-16", cycleId = "c1")
        )
        coEvery { dailyLogDao.getByCycleId("c1") } returns entities
        coEvery { trackingEntryDao.getByLogId(any()) } returns emptyList()

        val result = repository.getByCycleId("c1")

        assertEquals(2, result.size)
    }

    @Test
    fun `getByDateRange returns logs in range`() = runTest {
        val entities = listOf(
            DailyLogEntity(id = "log1", date = "2024-01-15"),
            DailyLogEntity(id = "log2", date = "2024-01-16"),
            DailyLogEntity(id = "log3", date = "2024-01-17")
        )
        coEvery { dailyLogDao.getByDateRange("2024-01-15", "2024-01-17") } returns entities
        coEvery { trackingEntryDao.getByLogId(any()) } returns emptyList()

        val result = repository.getByDateRange(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 1, 17))

        assertEquals(3, result.size)
    }

    @Test
    fun `insertOrUpdate delegates to dao`() = runTest {
        val log = DailyLog(
            id = "log1", date = LocalDate.of(2024, 1, 15),
            cycleId = "c1", cycleDay = 5, notes = "Test"
        )

        repository.insertOrUpdate(log)

        coVerify { dailyLogDao.insert(any()) }
    }

    @Test
    fun `saveTrackingEntries deletes old and inserts new`() = runTest {
        val entries = listOf(
            TrackingEntry(
                id = "e1", dailyLogId = "log1",
                category = TrackingCategory.PAIN, subcategory = "cramps"
            )
        )

        repository.saveTrackingEntries("log1", entries)

        coVerify { trackingEntryDao.deleteByLogId("log1") }
        coVerify { trackingEntryDao.insertAll(any()) }
    }

    @Test
    fun `saveTrackingEntries with empty list only deletes`() = runTest {
        repository.saveTrackingEntries("log1", emptyList())

        coVerify { trackingEntryDao.deleteByLogId("log1") }
        coVerify(exactly = 0) { trackingEntryDao.insertAll(any()) }
    }

    @Test
    fun `getTrackingEntries maps entities to domain`() = runTest {
        val entities = listOf(
            TrackingEntryEntity(
                id = "e1", dailyLogId = "log1",
                category = "mood", subcategory = "happy", intensity = 8
            ),
            TrackingEntryEntity(
                id = "e2", dailyLogId = "log1",
                category = "energy", subcategory = "high"
            )
        )
        coEvery { trackingEntryDao.getByLogId("log1") } returns entities

        val result = repository.getTrackingEntries("log1")

        assertEquals(2, result.size)
        assertEquals(TrackingCategory.MOOD, result[0].category)
        assertEquals("happy", result[0].subcategory)
        assertEquals(8, result[0].intensity)
        assertEquals(TrackingCategory.ENERGY, result[1].category)
    }

    @Test
    fun `intensity is clamped to valid range`() = runTest {
        val entities = listOf(
            TrackingEntryEntity(
                id = "e1", dailyLogId = "log1",
                category = "pain", subcategory = "cramps", intensity = 15
            )
        )
        coEvery { trackingEntryDao.getByLogId("log1") } returns entities

        val result = repository.getTrackingEntries("log1")

        assertEquals(1, result.size)
        assertEquals(10, result[0].intensity)
    }

    @Test
    fun `delete delegates to dao`() = runTest {
        val log = DailyLog(id = "log1", date = LocalDate.of(2024, 1, 15))
        repository.delete(log)
        coVerify { dailyLogDao.delete(any()) }
    }
}
