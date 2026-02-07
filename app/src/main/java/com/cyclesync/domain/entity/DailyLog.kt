package com.cyclesync.domain.entity

import java.time.LocalDate

data class DailyLog(
    val id: String,
    val date: LocalDate,
    val cycleId: String? = null,
    val cycleDay: Int? = null,
    val notes: String? = null,
    val entries: List<TrackingEntry> = emptyList()
) {
    val hasEntries: Boolean
        get() = entries.isNotEmpty()

    val hasNotes: Boolean
        get() = !notes.isNullOrBlank()

    val entryCount: Int
        get() = entries.size

    val trackedCategories: Set<TrackingCategory>
        get() = entries.map { it.category }.toSet()

    val categoryCount: Int
        get() = trackedCategories.size

    fun hasCategory(category: TrackingCategory): Boolean =
        entries.any { it.category == category }

    fun getEntriesForCategory(category: TrackingCategory): List<TrackingEntry> =
        entries.filter { it.category == category }

    val isToday: Boolean
        get() = date == LocalDate.now()
}
