package com.cyclesync.domain.entity

import java.time.LocalDate

data class Prediction(
    val id: String,
    val cycleId: String? = null,
    val type: PredictionType,
    val predictedDate: LocalDate,
    val confidence: Double,
    val lowerBound: LocalDate? = null,
    val upperBound: LocalDate? = null,
    val algorithmVersion: String,
    val isStale: Boolean = false
)

enum class PredictionType {
    PERIOD_START,
    PERIOD_END,
    PMS_START,
    PMS_END,
    FERTILE_START,
    FERTILE_END,
    OVULATION
}
