package com.cyclesync.core.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

object DateUtils {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val shortFormatter = DateTimeFormatter.ofPattern("MMM d")
    private val mediumFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    fun toIsoString(date: LocalDate): String = date.format(isoFormatter)

    fun fromIsoString(str: String): LocalDate = LocalDate.parse(str, isoFormatter)

    fun fromIsoStringOrNull(str: String?): LocalDate? {
        if (str.isNullOrBlank()) return null
        return try {
            LocalDate.parse(str, isoFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun daysBetween(start: LocalDate, end: LocalDate): Long =
        ChronoUnit.DAYS.between(start, end)

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun isFuture(date: LocalDate): Boolean = date.isAfter(LocalDate.now())

    fun isPast(date: LocalDate): Boolean = date.isBefore(LocalDate.now())

    fun isTodayOrFuture(date: LocalDate): Boolean = !date.isBefore(LocalDate.now())

    fun isTodayOrPast(date: LocalDate): Boolean = !date.isAfter(LocalDate.now())

    fun clampToRange(date: LocalDate, min: LocalDate, max: LocalDate): LocalDate {
        return when {
            date.isBefore(min) -> min
            date.isAfter(max) -> max
            else -> date
        }
    }

    fun formatShort(date: LocalDate): String = date.format(shortFormatter)

    fun formatMedium(date: LocalDate): String = date.format(mediumFormatter)

    fun generateDateRange(start: LocalDate, endInclusive: LocalDate): List<LocalDate> {
        if (start.isAfter(endInclusive)) return emptyList()
        val days = ChronoUnit.DAYS.between(start, endInclusive).toInt() + 1
        return (0 until days).map { start.plusDays(it.toLong()) }
    }
}
