package com.cyclesync.domain.prediction

import com.cyclesync.core.constants.PopulationPriors
import com.cyclesync.core.utils.CycleCalculator
import com.cyclesync.core.utils.UuidGenerator
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.Prediction
import com.cyclesync.domain.entity.PredictionType
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

class PredictionEngine {

    data class PredictionResult(
        val nextPeriodStart: LocalDate,
        val periodEnd: LocalDate,
        val ovulationDate: LocalDate,
        val fertileWindowStart: LocalDate,
        val fertileWindowEnd: LocalDate,
        val pmsStart: LocalDate,
        val pmsEnd: LocalDate,
        val confidence: Double,
        val lowerBound: LocalDate,
        val upperBound: LocalDate
    )

    fun predictNextCycle(
        cycles: List<Cycle>,
        userAge: Int?,
        userBmi: String?,
        currentCycleStartDate: LocalDate
    ): PredictionResult {
        val prior = PopulationPriors.getAdjustedPrior(userAge, userBmi)
        val validCycles = cycles.filter { !it.isExcluded && it.cycleLength != null }
        val n = validCycles.size

        // Step 1-2: Compute individual statistics
        val individualMean: Double
        val individualSD: Double
        val individualPeriodMean: Double

        if (n >= 1) {
            val lengths = validCycles.mapNotNull { it.cycleLength }
                .map { adjustForSkips(it, prior.cycleMean) }
            individualMean = lengths.average()
            individualSD = if (lengths.size >= 2) CycleCalculator.calculateStandardDeviation(lengths.map { it.roundToInt() }) else prior.cycleSD
            individualPeriodMean = CycleCalculator.calculateAveragePeriodLength(validCycles)
        } else {
            individualMean = prior.cycleMean
            individualSD = prior.cycleSD
            individualPeriodMean = PopulationPriors.POPULATION_MEAN_PERIOD
        }

        // Step 3: Bayesian weighting
        val wPop = max(PopulationPriors.MIN_POPULATION_WEIGHT, 1.0 - (n.toDouble() / PopulationPriors.TRANSITION_CYCLES))
        val wInd = 1.0 - wPop

        // Step 4: Blended prediction
        var predictedCycleLength = wPop * prior.cycleMean + wInd * individualMean
        val predictedVariance = wPop * (prior.cycleSD * prior.cycleSD) + wInd * (individualSD * individualSD)

        // Step 5: Trend adjustment for 8+ cycles
        if (n >= 8) {
            val recent = validCycles.takeLast(6).mapNotNull { it.cycleLength }
            if (recent.size >= 4) {
                val slope = linearRegressionSlope(recent)
                if (abs(slope) > 0.3) {
                    predictedCycleLength += slope * 0.5
                }
            }
        }

        // Step 6: Compute predicted dates
        val nextPeriodStart = currentCycleStartDate.plusDays(predictedCycleLength.roundToInt().toLong())

        val userLutealEstimate = if (n >= 3) {
            val est = individualMean - (individualMean - prior.lutealMean)
            est.coerceIn(7.0, 17.0)
        } else {
            prior.lutealMean
        }

        val ovulationDate = nextPeriodStart.minusDays(userLutealEstimate.roundToInt().toLong())
        val fertileWindowStart = ovulationDate.minusDays(PopulationPriors.FERTILE_WINDOW_START_BEFORE_OVULATION.toLong())
        val fertileWindowEnd = ovulationDate.plusDays(PopulationPriors.FERTILE_WINDOW_END_AFTER_OVULATION.toLong())
        val pmsStart = nextPeriodStart.minusDays(PopulationPriors.PMS_WINDOW_START_BEFORE_PERIOD.toLong())
        val pmsEnd = nextPeriodStart.minusDays(PopulationPriors.PMS_WINDOW_END_BEFORE_PERIOD.toLong())
        val periodEnd = nextPeriodStart.plusDays(individualPeriodMean.roundToInt().toLong() - 1)

        // Step 7: Confidence
        val confidence = calculateConfidence(n, predictedVariance, sqrt(predictedVariance))
        val sqrtVar = ceil(sqrt(predictedVariance)).toLong()
        val lowerBound = nextPeriodStart.minusDays(sqrtVar)
        val upperBound = nextPeriodStart.plusDays(sqrtVar)

        return PredictionResult(
            nextPeriodStart = nextPeriodStart,
            periodEnd = periodEnd,
            ovulationDate = ovulationDate,
            fertileWindowStart = fertileWindowStart,
            fertileWindowEnd = fertileWindowEnd,
            pmsStart = pmsStart,
            pmsEnd = pmsEnd,
            confidence = confidence,
            lowerBound = lowerBound,
            upperBound = upperBound
        )
    }

    fun generatePredictions(
        cycles: List<Cycle>,
        userAge: Int?,
        userBmi: String?,
        numberOfFutureCycles: Int = 12
    ): List<Prediction> {
        if (cycles.isEmpty()) return emptyList()

        val lastCycle = cycles.maxByOrNull { it.startDate } ?: return emptyList()
        val predictions = mutableListOf<Prediction>()
        var currentStartDate = lastCycle.startDate

        for (i in 0 until numberOfFutureCycles) {
            val result = predictNextCycle(cycles, userAge, userBmi, currentStartDate)

            predictions.addAll(listOf(
                createPrediction(PredictionType.PERIOD_START, result.nextPeriodStart, result.confidence, result.lowerBound, result.upperBound),
                createPrediction(PredictionType.PERIOD_END, result.periodEnd, result.confidence, result.lowerBound, result.upperBound),
                createPrediction(PredictionType.OVULATION, result.ovulationDate, result.confidence * 0.9, null, null),
                createPrediction(PredictionType.FERTILE_START, result.fertileWindowStart, result.confidence * 0.85, null, null),
                createPrediction(PredictionType.FERTILE_END, result.fertileWindowEnd, result.confidence * 0.85, null, null),
                createPrediction(PredictionType.PMS_START, result.pmsStart, result.confidence * 0.8, null, null),
                createPrediction(PredictionType.PMS_END, result.pmsEnd, result.confidence * 0.8, null, null)
            ))

            currentStartDate = result.nextPeriodStart
        }

        return predictions
    }

    private fun createPrediction(
        type: PredictionType,
        date: LocalDate,
        confidence: Double,
        lowerBound: LocalDate?,
        upperBound: LocalDate?
    ): Prediction = Prediction(
        id = UuidGenerator.generate(),
        type = type,
        predictedDate = date,
        confidence = confidence.coerceIn(0.1, 0.95),
        lowerBound = lowerBound,
        upperBound = upperBound,
        algorithmVersion = PopulationPriors.ALGORITHM_VERSION
    )

    private fun adjustForSkips(cycleLength: Int, priorMean: Double): Double {
        return if (cycleLength > priorMean * 1.8) {
            cycleLength.toDouble() / (cycleLength / priorMean).roundToInt()
        } else {
            cycleLength.toDouble()
        }
    }

    private fun calculateConfidence(cyclesTracked: Int, variance: Double, sd: Double): Double {
        val cycleConf = min(0.9, 0.3 + cyclesTracked * 0.08)
        val variancePenalty = min(0.3, sd / 15.0)
        return (cycleConf - variancePenalty).coerceIn(0.1, 0.95)
    }

    private fun linearRegressionSlope(values: List<Int>): Double {
        val n = values.size
        if (n < 2) return 0.0
        val xMean = (n - 1) / 2.0
        val yMean = values.average()
        var numerator = 0.0
        var denominator = 0.0
        for (i in values.indices) {
            numerator += (i - xMean) * (values[i] - yMean)
            denominator += (i - xMean) * (i - xMean)
        }
        return if (denominator != 0.0) numerator / denominator else 0.0
    }
}
