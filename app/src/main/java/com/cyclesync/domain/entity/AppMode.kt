package com.cyclesync.domain.entity

enum class AppMode(val displayName: String, val description: String) {
    PERIOD_TRACKING("Period Tracking", "Track your menstrual cycle and symptoms"),
    CONCEIVE("Conceive", "Track fertility and optimize conception timing"),
    PREGNANCY("Pregnancy", "Track your pregnancy journey"),
    PERIMENOPAUSE("Perimenopause", "Track perimenopause symptoms and changes");

    val showsFertilityData: Boolean
        get() = this == CONCEIVE || this == PERIOD_TRACKING

    val showsPregnancyData: Boolean
        get() = this == PREGNANCY

    fun next(): AppMode {
        val modes = entries
        return modes[(ordinal + 1) % modes.size]
    }
}
