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
)

enum class Trend { LENGTHENING, SHORTENING, STABLE }
