package com.cyclesync.domain.entity

import java.time.LocalDate

data class DailyLog(
    val id: String,
    val date: LocalDate,
    val cycleId: String? = null,
    val cycleDay: Int? = null,
    val notes: String? = null,
    val entries: List<TrackingEntry> = emptyList()
)
