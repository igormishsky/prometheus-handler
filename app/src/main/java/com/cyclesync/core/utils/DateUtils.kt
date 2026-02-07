package com.cyclesync.core.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateUtils {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun toIsoString(date: LocalDate): String = date.format(isoFormatter)

    fun fromIsoString(str: String): LocalDate = LocalDate.parse(str, isoFormatter)

    fun daysBetween(start: LocalDate, end: LocalDate): Long =
        ChronoUnit.DAYS.between(start, end)

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun isFuture(date: LocalDate): Boolean = date.isAfter(LocalDate.now())

    fun isPast(date: LocalDate): Boolean = date.isBefore(LocalDate.now())
}
