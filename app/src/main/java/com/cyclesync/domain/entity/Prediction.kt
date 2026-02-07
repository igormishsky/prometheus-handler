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
) {
    val isHighConfidence: Boolean
        get() = confidence >= 0.7

    val isMediumConfidence: Boolean
        get() = confidence in 0.4..0.7

    val isLowConfidence: Boolean
        get() = confidence < 0.4

    val confidencePercent: Int
        get() = (confidence * 100).toInt().coerceIn(0, 100)

    val isPastPrediction: Boolean
        get() = predictedDate.isBefore(LocalDate.now())

    val isFuturePrediction: Boolean
        get() = !predictedDate.isBefore(LocalDate.now())

    val uncertaintyDays: Long?
        get() {
            val lower = lowerBound ?: return null
            val upper = upperBound ?: return null
            return java.time.temporal.ChronoUnit.DAYS.between(lower, upper)
        }
}

enum class PredictionType(val displayName: String) {
    PERIOD_START("Period Start"),
    PERIOD_END("Period End"),
    PMS_START("PMS Start"),
    PMS_END("PMS End"),
    FERTILE_START("Fertile Window Start"),
    FERTILE_END("Fertile Window End"),
    OVULATION("Ovulation");

    companion object {
        fun fromNameOrNull(name: String?): PredictionType? {
            if (name.isNullOrBlank()) return null
            return try {
                valueOf(name.uppercase())
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}
