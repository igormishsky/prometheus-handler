package com.cyclesync.domain.entity

import java.time.LocalDate

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
    val notes: String? = null
)
