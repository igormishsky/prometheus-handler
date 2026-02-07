package com.cyclesync.domain.entity

import java.time.LocalDate

data class Pregnancy(
    val id: String,
    val lmpDate: LocalDate? = null,
    val dueDate: LocalDate,
    val conceptionDate: LocalDate? = null,
    val startedAt: LocalDate = LocalDate.now(),
    val endedAt: LocalDate? = null,
    val outcome: PregnancyOutcome? = null,
    val notes: String? = null
)

enum class PregnancyOutcome {
    BIRTH, MISCARRIAGE, OTHER
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
)
