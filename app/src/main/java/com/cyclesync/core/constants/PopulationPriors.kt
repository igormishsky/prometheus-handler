package com.cyclesync.core.constants

data class CyclePrior(
    val cycleMean: Double,
    val cycleSD: Double,
    val follicularMean: Double,
    val lutealMean: Double
)

object PopulationPriors {
    // Mean cycle length in days by age group (Bull 2019, 612K cycles)
    val agePriors = mapOf(
        "15-19" to CyclePrior(cycleMean = 30.8, cycleSD = 9.2, follicularMean = 18.2, lutealMean = 12.6),
        "20-24" to CyclePrior(cycleMean = 30.1, cycleSD = 8.1, follicularMean = 17.5, lutealMean = 12.6),
        "25-29" to CyclePrior(cycleMean = 29.5, cycleSD = 7.2, follicularMean = 17.0, lutealMean = 12.5),
        "30-34" to CyclePrior(cycleMean = 29.1, cycleSD = 6.8, follicularMean = 16.7, lutealMean = 12.4),
        "35-39" to CyclePrior(cycleMean = 28.5, cycleSD = 6.0, follicularMean = 16.1, lutealMean = 12.4),
        "40-44" to CyclePrior(cycleMean = 27.8, cycleSD = 6.5, follicularMean = 15.5, lutealMean = 12.3),
        "45-50" to CyclePrior(cycleMean = 28.4, cycleSD = 9.8, follicularMean = 16.0, lutealMean = 12.4)
    )

    // BMI adjustments to cycle length (Bull 2019)
    val bmiCycleAdjustment = mapOf(
        "underweight" to 0.5,
        "normal" to 0.0,
        "overweight" to 0.3,
        "obese" to 0.9
    )

    val bmiVarianceMultiplier = mapOf(
        "underweight" to 1.1,
        "normal" to 1.0,
        "overweight" to 1.15,
        "obese" to 1.3
    )

    // Population constants from literature
    const val POPULATION_MEAN_CYCLE = 29.3          // Bull 2019
    const val POPULATION_SD_CYCLE = 7.5              // Bull 2019
    const val POPULATION_MEAN_FOLLICULAR = 16.9      // Bull 2019
    const val POPULATION_MEAN_LUTEAL = 12.4          // Bull 2019
    const val POPULATION_SD_LUTEAL = 2.4             // Bull 2019
    const val POPULATION_MEAN_PERIOD = 5.0           // Treloar 1967 + Bull 2019
    const val POPULATION_MEAN_PERIOD_SD = 1.8        // Treloar 1967

    // Fertile window (Wilcox 2000, NEJM)
    const val FERTILE_WINDOW_START_BEFORE_OVULATION = 5
    const val FERTILE_WINDOW_END_AFTER_OVULATION = 1

    // PMS window (ACOG guidelines)
    const val PMS_WINDOW_START_BEFORE_PERIOD = 7
    const val PMS_WINDOW_END_BEFORE_PERIOD = 1

    // Per-day conception probability (Wilcox 2000)
    val conceptionProbabilityByDay = mapOf(
        -5 to 0.04,
        -4 to 0.13,
        -3 to 0.08,
        -2 to 0.29,
        -1 to 0.27,
        0 to 0.08
    )

    // Bayesian transition parameters
    const val TRANSITION_CYCLES = 6
    const val MIN_POPULATION_WEIGHT = 0.05
    const val ALGORITHM_VERSION = "1.0"

    fun getPriorForAge(age: Int): CyclePrior {
        return when {
            age < 20 -> agePriors["15-19"]!!
            age < 25 -> agePriors["20-24"]!!
            age < 30 -> agePriors["25-29"]!!
            age < 35 -> agePriors["30-34"]!!
            age < 40 -> agePriors["35-39"]!!
            age < 45 -> agePriors["40-44"]!!
            else -> agePriors["45-50"]!!
        }
    }

    fun getAdjustedPrior(age: Int?, bmiCategory: String?): CyclePrior {
        val basePrior = if (age != null) getPriorForAge(age) else {
            CyclePrior(POPULATION_MEAN_CYCLE, POPULATION_SD_CYCLE, POPULATION_MEAN_FOLLICULAR, POPULATION_MEAN_LUTEAL)
        }

        val bmiAdj = bmiCycleAdjustment[bmiCategory] ?: 0.0
        val bmiVar = bmiVarianceMultiplier[bmiCategory] ?: 1.0

        return CyclePrior(
            cycleMean = basePrior.cycleMean + bmiAdj,
            cycleSD = basePrior.cycleSD * bmiVar,
            follicularMean = basePrior.follicularMean + bmiAdj,
            lutealMean = basePrior.lutealMean
        )
    }
}
