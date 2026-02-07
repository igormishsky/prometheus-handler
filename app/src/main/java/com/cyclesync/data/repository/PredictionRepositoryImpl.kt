package com.cyclesync.data.repository

import com.cyclesync.core.database.dao.PredictionDao
import com.cyclesync.core.database.entity.PredictionEntity
import com.cyclesync.core.utils.DateUtils
import com.cyclesync.domain.entity.Prediction
import com.cyclesync.domain.entity.PredictionType
import com.cyclesync.domain.repository.PredictionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionRepositoryImpl @Inject constructor(
    private val predictionDao: PredictionDao
) : PredictionRepository {

    override fun getActivePredictions(): Flow<List<Prediction>> {
        return predictionDao.getActivePredictions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActivePredictionsOnce(): List<Prediction> {
        return predictionDao.getActivePredictionsOnce().map { it.toDomain() }
    }

    override suspend fun getByType(type: PredictionType): List<Prediction> {
        return predictionDao.getByType(type.name.lowercase()).map { it.toDomain() }
    }

    override suspend fun getByCycleId(cycleId: String): List<Prediction> {
        return predictionDao.getByCycleId(cycleId).map { it.toDomain() }
    }

    override suspend fun savePredictions(predictions: List<Prediction>) {
        predictionDao.markAllStale()
        predictionDao.insertAll(predictions.map { it.toEntity() })
        predictionDao.deleteStalePredictions()
    }

    override suspend fun markAllStale() {
        predictionDao.markAllStale()
    }

    override suspend fun deleteAll() {
        predictionDao.deleteAll()
    }

    private fun PredictionEntity.toDomain(): Prediction = Prediction(
        id = id,
        cycleId = cycleId,
        type = try { PredictionType.valueOf(type.uppercase()) } catch (_: Exception) { PredictionType.PERIOD_START },
        predictedDate = DateUtils.fromIsoString(predictedDate),
        confidence = confidence,
        lowerBound = lowerBound?.let { DateUtils.fromIsoString(it) },
        upperBound = upperBound?.let { DateUtils.fromIsoString(it) },
        algorithmVersion = algorithmVersion,
        isStale = isStale
    )

    private fun Prediction.toEntity(): PredictionEntity = PredictionEntity(
        id = id,
        cycleId = cycleId,
        type = type.name.lowercase(),
        predictedDate = DateUtils.toIsoString(predictedDate),
        confidence = confidence,
        lowerBound = lowerBound?.let { DateUtils.toIsoString(it) },
        upperBound = upperBound?.let { DateUtils.toIsoString(it) },
        algorithmVersion = algorithmVersion,
        generatedAt = DateUtils.toIsoString(LocalDate.now()),
        isStale = isStale
    )
}
