package com.cyclesync.domain.entity

enum class CyclePhase(val displayName: String, val description: String) {
    MENSTRUATION("Menstruation", "Your body is shedding the uterine lining"),
    FOLLICULAR("Follicular", "Estrogen rises as follicles develop"),
    OVULATION("Ovulation", "An egg is released from the ovary"),
    LUTEAL("Luteal", "Progesterone rises to prepare for potential pregnancy"),
    PMS("PMS", "Hormone levels drop before your next period");

    val isBleedingPhase: Boolean
        get() = this == MENSTRUATION

    val isFertilePhase: Boolean
        get() = this == OVULATION
}
