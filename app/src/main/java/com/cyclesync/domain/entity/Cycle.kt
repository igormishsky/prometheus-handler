package com.cyclesync.domain.entity

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Cycle(
    val id: String,
    val cycleNumber: Int,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val periodStartDate: LocalDate,
    val periodEndDate: LocalDate? = null,
    val cycleLength: Int? = null,
    val periodLength: Int? = null,
    val isExcluded: Boolean = false,
    val notes: String? = null,
    val skipReason: SkipReason? = null
) {
    val isOpen: Boolean
        get() = endDate == null

    val isComplete: Boolean
        get() = endDate != null && cycleLength != null

    val hasNotes: Boolean
        get() = !notes.isNullOrBlank()

    val durationDays: Long?
        get() = endDate?.let { ChronoUnit.DAYS.between(startDate, it) }

    val effectivePeriodEnd: LocalDate
        get() = periodEndDate ?: periodStartDate.plusDays((periodLength ?: 5).toLong() - 1)

    val effectiveCycleLength: Int
        get() = cycleLength ?: 28

    fun isDateInPeriod(date: LocalDate): Boolean {
        return !date.isBefore(periodStartDate) && !date.isAfter(effectivePeriodEnd)
    }

    fun isDateInCycle(date: LocalDate): Boolean {
        val end = endDate ?: startDate.plusDays(effectiveCycleLength.toLong())
        return !date.isBefore(startDate) && !date.isAfter(end)
    }
}

enum class SkipReason(val displayName: String) {
    ILLNESS("Illness / Infection"),
    STRESS("Stress"),
    MEDICATION("Medication / Birth control"),
    WEIGHT_CHANGE("Significant weight change"),
    TRAVEL("Travel / Timezone change"),
    BREASTFEEDING("Breastfeeding"),
    PCOS("PCOS / Hormonal condition"),
    OTHER("Other");

    companion object {
        fun fromNameOrNull(name: String?): SkipReason? {
            if (name.isNullOrBlank()) return null
            return try {
                valueOf(name.uppercase())
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }
}
