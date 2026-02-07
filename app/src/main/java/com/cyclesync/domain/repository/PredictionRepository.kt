package com.cyclesync.domain.repository

import com.cyclesync.domain.entity.Prediction
import com.cyclesync.domain.entity.PredictionType
import kotlinx.coroutines.flow.Flow

interface PredictionRepository {
    fun getActivePredictions(): Flow<List<Prediction>>
    suspend fun getActivePredictionsOnce(): List<Prediction>
    suspend fun getByType(type: PredictionType): List<Prediction>
    suspend fun getByCycleId(cycleId: String): List<Prediction>
    suspend fun savePredictions(predictions: List<Prediction>)
    suspend fun markAllStale()
    suspend fun deleteAll()
}
