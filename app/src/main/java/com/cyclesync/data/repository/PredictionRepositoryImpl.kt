package com.cyclesync.data.repository

import com.cyclesync.core.database.dao.PredictionDao
import com.cyclesync.core.database.entity.PredictionEntity
import com.cyclesync.core.utils.DateUtils
import com.cyclesync.domain.entity.Prediction
import com.cyclesync.domain.entity.PredictionType
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.core.utils.enumValueOfOrNull
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
            entities.mapNotNull { it.toDomainOrNull() }
        }
    }

    override suspend fun getActivePredictionsOnce(): List<Prediction> {
        return predictionDao.getActivePredictionsOnce().mapNotNull { it.toDomainOrNull() }
    }

    override suspend fun getByType(type: PredictionType): List<Prediction> {
        return predictionDao.getByType(type.name.lowercase()).mapNotNull { it.toDomainOrNull() }
    }

    override suspend fun getByCycleId(cycleId: String): List<Prediction> {
        return predictionDao.getByCycleId(cycleId).mapNotNull { it.toDomainOrNull() }
    }

    override suspend fun savePredictions(predictions: List<Prediction>) {
        predictionDao.markAllStale()
        if (predictions.isNotEmpty()) {
            predictionDao.insertAll(predictions.map { it.toEntity() })
        }
        predictionDao.deleteStalePredictions()
    }

    override suspend fun markAllStale() {
        predictionDao.markAllStale()
    }

    override suspend fun deleteAll() {
        predictionDao.deleteAll()
    }

    private fun PredictionEntity.toDomainOrNull(): Prediction? {
        val parsedType = enumValueOfOrNull<PredictionType>(type) ?: return null
        val parsedDate = DateUtils.fromIsoStringOrNull(predictedDate) ?: return null
        return Prediction(
            id = id,
            cycleId = cycleId,
            type = parsedType,
            predictedDate = parsedDate,
            confidence = confidence.coerceIn(0.0, 1.0),
            lowerBound = lowerBound?.let { DateUtils.fromIsoStringOrNull(it) },
            upperBound = upperBound?.let { DateUtils.fromIsoStringOrNull(it) },
            algorithmVersion = algorithmVersion,
            isStale = isStale
        )
    }

    private fun Prediction.toEntity(): PredictionEntity = PredictionEntity(
        id = id,
        cycleId = cycleId,
        type = type.name.lowercase(),
        predictedDate = DateUtils.toIsoString(predictedDate),
        confidence = confidence.coerceIn(0.0, 1.0),
        lowerBound = lowerBound?.let { DateUtils.toIsoString(it) },
        upperBound = upperBound?.let { DateUtils.toIsoString(it) },
        algorithmVersion = algorithmVersion,
        generatedAt = DateUtils.toIsoString(LocalDate.now()),
        isStale = isStale
    )
}
