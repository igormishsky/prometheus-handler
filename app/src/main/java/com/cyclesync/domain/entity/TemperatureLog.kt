package com.cyclesync.domain.entity

import java.time.LocalDate
import java.time.LocalTime

data class TemperatureLog(
    val id: String,
    val date: LocalDate,
    val temperature: Double,
    val timeTaken: LocalTime? = null,
    val method: TemperatureMethod = TemperatureMethod.ORAL,
    val isFlagged: Boolean = false,
    val dailyLogId: String? = null
)

enum class TemperatureMethod(val displayName: String) {
    ORAL("Oral"),
    VAGINAL("Vaginal"),
    WEARABLE("Wearable"),
    EAR("Ear")
}
