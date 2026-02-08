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
) {
    val temperatureFahrenheit: Double
        get() = temperature * 9.0 / 5.0 + 32.0

    val isInNormalRange: Boolean
        get() = temperature in 35.5..37.8

    val isElevated: Boolean
        get() = temperature > 37.0
}

enum class TemperatureMethod(val displayName: String) {
    ORAL("Oral"),
    VAGINAL("Vaginal"),
    WEARABLE("Wearable"),
    EAR("Ear");
}
