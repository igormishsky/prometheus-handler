package com.cyclesync.data.repository

import com.cyclesync.core.database.dao.CycleDao
import com.cyclesync.core.database.entity.CycleEntity
import com.cyclesync.domain.entity.Cycle
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CycleRepositoryImplTest {

    @MockK
    private lateinit var cycleDao: CycleDao

    private lateinit var repository: CycleRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = CycleRepositoryImpl(cycleDao)
    }

    // --- Entity to Domain mapping ---

    @Test
    fun `getAllCycles maps entities to domain objects`() = runTest {
        val entity = CycleEntity(
            id = "test-1",
            cycleNumber = 1,
            startDate = "2024-03-01",
            endDate = "2024-03-28",
            periodStartDate = "2024-03-01",
            periodEndDate = "2024-03-05",
            cycleLength = 28,
            periodLength = 5,
            isExcluded = false,
            notes = "test note",
            createdAt = "2024-03-01",
            updatedAt = "2024-03-01"
        )
        coEvery { cycleDao.getAllCycles() } returns flowOf(listOf(entity))

        val cycles = repository.getAllCycles().first()

        assertEquals(1, cycles.size)
        val cycle = cycles[0]
        assertEquals("test-1", cycle.id)
        assertEquals(1, cycle.cycleNumber)
        assertEquals(LocalDate.of(2024, 3, 1), cycle.startDate)
        assertEquals(LocalDate.of(2024, 3, 28), cycle.endDate)
        assertEquals(LocalDate.of(2024, 3, 1), cycle.periodStartDate)
        assertEquals(LocalDate.of(2024, 3, 5), cycle.periodEndDate)
        assertEquals(28, cycle.cycleLength)
        assertEquals(5, cycle.periodLength)
        assertFalse(cycle.isExcluded)
        assertEquals("test note", cycle.notes)
    }

    @Test
    fun `getAllCycles maps null optional fields correctly`() = runTest {
        val entity = CycleEntity(
            id = "test-2",
            cycleNumber = 1,
            startDate = "2024-03-01",
            endDate = null,
            periodStartDate = "2024-03-01",
            periodEndDate = null,
            cycleLength = null,
            periodLength = null,
            isExcluded = false,
            notes = null
        )
        coEvery { cycleDao.getAllCycles() } returns flowOf(listOf(entity))

        val cycles = repository.getAllCycles().first()
        val cycle = cycles[0]

        assertNull(cycle.endDate)
        assertNull(cycle.periodEndDate)
        assertNull(cycle.cycleLength)
        assertNull(cycle.periodLength)
        assertNull(cycle.notes)
    }

    @Test
    fun `getAllCycles returns empty list for no data`() = runTest {
        coEvery { cycleDao.getAllCycles() } returns flowOf(emptyList())

        val cycles = repository.getAllCycles().first()
        assertTrue(cycles.isEmpty())
    }

    // --- Domain to Entity mapping ---

    @Test
    fun `insertCycle maps domain to entity correctly`() = runTest {
        val cycle = Cycle(
            id = "cycle-1",
            cycleNumber = 3,
            startDate = LocalDate.of(2024, 5, 10),
            endDate = LocalDate.of(2024, 6, 6),
            periodStartDate = LocalDate.of(2024, 5, 10),
            periodEndDate = LocalDate.of(2024, 5, 15),
            cycleLength = 28,
            periodLength = 6,
            isExcluded = true,
            notes = "excluded for irregular"
        )

        coEvery { cycleDao.insert(any()) } just Runs

        repository.insertCycle(cycle)

        coVerify {
            cycleDao.insert(match { entity ->
                entity.id == "cycle-1" &&
                entity.cycleNumber == 3 &&
                entity.startDate == "2024-05-10" &&
                entity.endDate == "2024-06-06" &&
                entity.periodStartDate == "2024-05-10" &&
                entity.periodEndDate == "2024-05-15" &&
                entity.cycleLength == 28 &&
                entity.periodLength == 6 &&
                entity.isExcluded &&
                entity.notes == "excluded for irregular"
            })
        }
    }

    @Test
    fun `insertCycle handles null optional fields`() = runTest {
        val cycle = Cycle(
            id = "cycle-2",
            cycleNumber = 1,
            startDate = LocalDate.of(2024, 5, 1),
            periodStartDate = LocalDate.of(2024, 5, 1)
        )

        coEvery { cycleDao.insert(any()) } just Runs

        repository.insertCycle(cycle)

        coVerify {
            cycleDao.insert(match { entity ->
                entity.endDate == null &&
                entity.periodEndDate == null &&
                entity.cycleLength == null &&
                entity.periodLength == null &&
                !entity.isExcluded &&
                entity.notes == null
            })
        }
    }

    // --- getCycleById ---

    @Test
    fun `getCycleById returns mapped cycle when found`() = runTest {
        val entity = CycleEntity(
            id = "test-id",
            cycleNumber = 1,
            startDate = "2024-01-01",
            periodStartDate = "2024-01-01"
        )
        coEvery { cycleDao.getCycleById("test-id") } returns entity

        val cycle = repository.getCycleById("test-id")

        assertNotNull(cycle)
        assertEquals("test-id", cycle?.id)
        assertEquals(LocalDate.of(2024, 1, 1), cycle?.startDate)
    }

    @Test
    fun `getCycleById returns null when not found`() = runTest {
        coEvery { cycleDao.getCycleById("nonexistent") } returns null

        val cycle = repository.getCycleById("nonexistent")
        assertNull(cycle)
    }

    // --- deleteAllCycles ---

    @Test
    fun `deleteAllCycles delegates to DAO`() = runTest {
        coEvery { cycleDao.deleteAll() } just Runs

        repository.deleteAllCycles()

        coVerify { cycleDao.deleteAll() }
    }

    // --- getCycleCount ---

    @Test
    fun `getCycleCount delegates to DAO`() = runTest {
        coEvery { cycleDao.getCycleCount() } returns 5

        assertEquals(5, repository.getCycleCount())
    }
}
