package com.cyclesync.data.repository

import com.cyclesync.core.database.dao.PredictionDao
import com.cyclesync.core.database.entity.PredictionEntity
import com.cyclesync.domain.entity.Prediction
import com.cyclesync.domain.entity.PredictionType
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class PredictionRepositoryImplTest {

    @MockK
    private lateinit var predictionDao: PredictionDao

    private lateinit var repository: PredictionRepositoryImpl

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = PredictionRepositoryImpl(predictionDao)
    }

    // --- Entity to Domain mapping ---

    @Test
    fun `getActivePredictions maps entity to domain correctly`() = runTest {
        val entity = PredictionEntity(
            id = "pred-1",
            cycleId = "cycle-1",
            type = "period_start",
            predictedDate = "2024-04-01",
            confidence = 0.85,
            lowerBound = "2024-03-30",
            upperBound = "2024-04-03",
            algorithmVersion = "1.0",
            generatedAt = "2024-03-01",
            isStale = false
        )
        coEvery { predictionDao.getActivePredictions() } returns flowOf(listOf(entity))

        val predictions = repository.getActivePredictions().first()

        assertEquals(1, predictions.size)
        val pred = predictions[0]
        assertEquals("pred-1", pred.id)
        assertEquals("cycle-1", pred.cycleId)
        assertEquals(PredictionType.PERIOD_START, pred.type)
        assertEquals(LocalDate.of(2024, 4, 1), pred.predictedDate)
        assertEquals(0.85, pred.confidence, 0.001)
        assertEquals(LocalDate.of(2024, 3, 30), pred.lowerBound)
        assertEquals(LocalDate.of(2024, 4, 3), pred.upperBound)
        assertEquals("1.0", pred.algorithmVersion)
        assertFalse(pred.isStale)
    }

    @Test
    fun `entity with unknown type falls back to PERIOD_START`() = runTest {
        val entity = PredictionEntity(
            id = "pred-2",
            type = "unknown_type",
            predictedDate = "2024-04-01",
            confidence = 0.5,
            algorithmVersion = "1.0"
        )
        coEvery { predictionDao.getActivePredictions() } returns flowOf(listOf(entity))

        val predictions = repository.getActivePredictions().first()
        assertEquals(PredictionType.PERIOD_START, predictions[0].type)
    }

    @Test
    fun `entity with null optional fields maps correctly`() = runTest {
        val entity = PredictionEntity(
            id = "pred-3",
            cycleId = null,
            type = "ovulation",
            predictedDate = "2024-04-15",
            confidence = 0.7,
            lowerBound = null,
            upperBound = null,
            algorithmVersion = "1.0"
        )
        coEvery { predictionDao.getActivePredictions() } returns flowOf(listOf(entity))

        val predictions = repository.getActivePredictions().first()
        val pred = predictions[0]
        assertNull(pred.cycleId)
        assertNull(pred.lowerBound)
        assertNull(pred.upperBound)
        assertEquals(PredictionType.OVULATION, pred.type)
    }

    @Test
    fun `all prediction types map correctly from lowercase strings`() = runTest {
        val types = mapOf(
            "period_start" to PredictionType.PERIOD_START,
            "period_end" to PredictionType.PERIOD_END,
            "ovulation" to PredictionType.OVULATION,
            "fertile_start" to PredictionType.FERTILE_START,
            "fertile_end" to PredictionType.FERTILE_END,
            "pms_start" to PredictionType.PMS_START,
            "pms_end" to PredictionType.PMS_END
        )

        for ((entityType, expectedDomainType) in types) {
            val entity = PredictionEntity(
                id = "pred-$entityType",
                type = entityType,
                predictedDate = "2024-04-01",
                confidence = 0.5,
                algorithmVersion = "1.0"
            )
            coEvery { predictionDao.getActivePredictions() } returns flowOf(listOf(entity))

            val predictions = repository.getActivePredictions().first()
            assertEquals("Type $entityType should map to $expectedDomainType",
                expectedDomainType, predictions[0].type)
        }
    }

    // --- Domain to Entity mapping ---

    @Test
    fun `savePredictions maps domain to entity and marks stale first`() = runTest {
        val prediction = Prediction(
            id = "pred-new",
            cycleId = "cycle-1",
            type = PredictionType.FERTILE_START,
            predictedDate = LocalDate.of(2024, 4, 10),
            confidence = 0.75,
            lowerBound = null,
            upperBound = null,
            algorithmVersion = "1.0"
        )

        coEvery { predictionDao.markAllStale() } just Runs
        coEvery { predictionDao.insertAll(any()) } just Runs
        coEvery { predictionDao.deleteStalePredictions() } just Runs

        repository.savePredictions(listOf(prediction))

        coVerifyOrder {
            predictionDao.markAllStale()
            predictionDao.insertAll(match { entities ->
                entities.size == 1 &&
                entities[0].id == "pred-new" &&
                entities[0].type == "fertile_start" &&
                entities[0].predictedDate == "2024-04-10" &&
                entities[0].confidence == 0.75
            })
            predictionDao.deleteStalePredictions()
        }
    }

    @Test
    fun `savePredictions with empty list still marks stale and cleans up`() = runTest {
        coEvery { predictionDao.markAllStale() } just Runs
        coEvery { predictionDao.insertAll(any()) } just Runs
        coEvery { predictionDao.deleteStalePredictions() } just Runs

        repository.savePredictions(emptyList())

        coVerifyOrder {
            predictionDao.markAllStale()
            predictionDao.insertAll(match { it.isEmpty() })
            predictionDao.deleteStalePredictions()
        }
    }

    // --- getByType ---

    @Test
    fun `getByType queries DAO with lowercase type name`() = runTest {
        coEvery { predictionDao.getByType("period_start") } returns listOf(
            PredictionEntity(
                id = "p1",
                type = "period_start",
                predictedDate = "2024-04-01",
                confidence = 0.8,
                algorithmVersion = "1.0"
            )
        )

        val result = repository.getByType(PredictionType.PERIOD_START)
        assertEquals(1, result.size)
        assertEquals(PredictionType.PERIOD_START, result[0].type)

        coVerify { predictionDao.getByType("period_start") }
    }

    // --- markAllStale ---

    @Test
    fun `markAllStale delegates to DAO`() = runTest {
        coEvery { predictionDao.markAllStale() } just Runs

        repository.markAllStale()

        coVerify { predictionDao.markAllStale() }
    }

    // --- deleteAll ---

    @Test
    fun `deleteAll delegates to DAO`() = runTest {
        coEvery { predictionDao.deleteAll() } just Runs

        repository.deleteAll()

        coVerify { predictionDao.deleteAll() }
    }
}
