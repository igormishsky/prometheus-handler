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
)

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
)

enum class MedicationStatus { TAKEN, MISSED, LATE, SKIPPED }
