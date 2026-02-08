package com.cyclesync.domain.entity

import java.time.LocalDate

data class Settings(
    val id: Int = 1,
    val activeMode: AppMode = AppMode.PERIOD_TRACKING,
    val birthYear: Int? = null,
    val bmiCategory: BmiCategory? = null,
    val typicalCycleLength: Int = 28,
    val typicalPeriodLength: Int = 5,
    val pinHash: String? = null,
    val appIcon: String = "default",
    val theme: AppTheme = AppTheme.SYSTEM,
    val unitsWeight: String = "kg",
    val unitsTemp: String = "celsius",
    val firstLaunchDate: LocalDate = LocalDate.now(),
    val lastBackupDate: LocalDate? = null,
    val onboardingCompleted: Boolean = false,
    val notificationPrivacy: NotificationPrivacy = NotificationPrivacy.HIGH
) {
    val currentAge: Int?
        get() = birthYear?.let { LocalDate.now().year - it }

    val usesCelsius: Boolean
        get() = unitsTemp == "celsius"

    val usesKg: Boolean
        get() = unitsWeight == "kg"
}

enum class BmiCategory(val displayName: String) {
    UNDERWEIGHT("Underweight"),
    NORMAL("Normal"),
    OVERWEIGHT("Overweight"),
    OBESE("Obese");
}

enum class AppTheme {
    SYSTEM, LIGHT, DARK;
}

enum class NotificationPrivacy(val displayName: String) {
    HIGH("High \u2014 No details"),
    MEDIUM("Medium \u2014 Vague"),
    LOW("Low \u2014 Full detail");
}
