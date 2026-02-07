package com.cyclesync.domain.entity

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Pregnancy(
    val id: String,
    val lmpDate: LocalDate? = null,
    val dueDate: LocalDate,
    val conceptionDate: LocalDate? = null,
    val startedAt: LocalDate = LocalDate.now(),
    val endedAt: LocalDate? = null,
    val outcome: PregnancyOutcome? = null,
    val notes: String? = null
) {
    val isOngoing: Boolean
        get() = endedAt == null && outcome == null

    val gestationalWeeks: Int?
        get() {
            val referenceDate = lmpDate ?: conceptionDate?.minusDays(14) ?: return null
            val days = ChronoUnit.DAYS.between(referenceDate, LocalDate.now())
            return (days / 7).toInt()
        }

    val gestationalDays: Int?
        get() {
            val referenceDate = lmpDate ?: conceptionDate?.minusDays(14) ?: return null
            val days = ChronoUnit.DAYS.between(referenceDate, LocalDate.now())
            return (days % 7).toInt()
        }

    val trimester: Int?
        get() {
            val weeks = gestationalWeeks ?: return null
            return when {
                weeks < 13 -> 1
                weeks < 27 -> 2
                else -> 3
            }
        }

    val daysUntilDue: Long
        get() = ChronoUnit.DAYS.between(LocalDate.now(), dueDate)
}

enum class PregnancyOutcome(val displayName: String) {
    BIRTH("Birth"),
    MISCARRIAGE("Miscarriage"),
    OTHER("Other")
}

data class PregnancyLog(
    val id: String,
    val pregnancyId: String,
    val date: LocalDate,
    val gestationalWeek: Int? = null,
    val gestationalDay: Int? = null,
    val category: String,
    val subcategory: String? = null,
    val value: String? = null,
    val notes: String? = null
) {
    val hasNotes: Boolean
        get() = !notes.isNullOrBlank()

    val gestationalAge: String?
        get() {
            val week = gestationalWeek ?: return null
            val day = gestationalDay ?: 0
            return "${week}w${day}d"
        }
}
