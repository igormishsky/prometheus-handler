package com.cyclesync.domain.entity

data class CycleAnalysis(
    val averageCycleLength: Double,
    val averagePeriodLength: Double,
    val cycleLengthVariability: Double,
    val longestCycle: Int,
    val shortestCycle: Int,
    val totalCyclesTracked: Int,
    val currentStreak: Int,
    val regularityScore: Int,
    val cycleLengthTrend: Trend = Trend.STABLE,
    val cycleLengths: List<Int> = emptyList()
) {
    val cycleLengthRange: Int
        get() = longestCycle - shortestCycle

    val isRegular: Boolean
        get() = regularityScore >= 7

    val isIrregular: Boolean
        get() = regularityScore <= 3

    val hasEnoughDataForTrend: Boolean
        get() = cycleLengths.size >= 6

    val regularityDescription: String
        get() = when {
            regularityScore >= 9 -> "Very Regular"
            regularityScore >= 7 -> "Regular"
            regularityScore >= 5 -> "Somewhat Regular"
            regularityScore >= 3 -> "Irregular"
            else -> "Very Irregular"
        }

    val trendDescription: String
        get() = when (cycleLengthTrend) {
            Trend.LENGTHENING -> "Cycles are getting longer"
            Trend.SHORTENING -> "Cycles are getting shorter"
            Trend.STABLE -> "Cycles are stable"
        }
}

enum class Trend(val displayName: String) {
    LENGTHENING("Lengthening"),
    SHORTENING("Shortening"),
    STABLE("Stable")
}
