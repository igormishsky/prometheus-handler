package com.cyclesync.domain.entity

import java.time.LocalDate
import java.time.LocalTime

data class Medication(
    val id: String,
    val name: String,
    val type: MedicationType,
    val dosage: String? = null,
    val frequency: MedicationFrequency = MedicationFrequency.DAILY,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true
) {
    val isCurrentlyActive: Boolean
        get() {
            if (!isActive) return false
            val today = LocalDate.now()
            val afterStart = startDate?.let { !today.isBefore(it) } ?: true
            val beforeEnd = endDate?.let { !today.isAfter(it) } ?: true
            return afterStart && beforeEnd
        }

    val hasDosage: Boolean
        get() = !dosage.isNullOrBlank()

    val hasDateRange: Boolean
        get() = startDate != null || endDate != null
}

enum class MedicationType(val displayName: String) {
    BIRTH_CONTROL_PILL("Birth Control Pill"),
    IUD("IUD"),
    PATCH("Patch"),
    RING("Ring"),
    INJECTION("Injection"),
    IMPLANT("Implant"),
    SUPPLEMENT("Supplement"),
    PRESCRIPTION("Prescription"),
    OTHER("Other")
}

enum class MedicationFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    AS_NEEDED("As Needed")
}

data class MedicationLog(
    val id: String,
    val medicationId: String,
    val date: LocalDate,
    val status: MedicationStatus,
    val timeTaken: LocalTime? = null,
    val notes: String? = null
) {
    val wasTaken: Boolean
        get() = status == MedicationStatus.TAKEN || status == MedicationStatus.LATE

    val wasMissed: Boolean
        get() = status == MedicationStatus.MISSED
}

enum class MedicationStatus(val displayName: String) {
    TAKEN("Taken"),
    MISSED("Missed"),
    LATE("Late"),
    SKIPPED("Skipped")
}
