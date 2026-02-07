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

    companion object {
        const val MIN_PREDICTED_CYCLE_LENGTH = 15.0
        const val MAX_PREDICTED_CYCLE_LENGTH = 60.0
        const val MIN_CONFIDENCE = 0.1
        const val MAX_CONFIDENCE = 0.95
        const val TREND_THRESHOLD = 0.3
        const val TREND_ADJUSTMENT_FACTOR = 0.5
        const val MIN_CYCLES_FOR_TREND = 8
        const val MIN_RECENT_CYCLES_FOR_TREND = 4
        const val RECENT_CYCLE_COUNT = 6
        const val MIN_CYCLES_FOR_INDIVIDUAL_LUTEAL = 3
        const val DEFAULT_FUTURE_CYCLES = 12
        const val PREDICTIONS_PER_CYCLE = 7
    }

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

        val wPop = max(PopulationPriors.MIN_POPULATION_WEIGHT, 1.0 - (n.toDouble() / PopulationPriors.TRANSITION_CYCLES))
        val wInd = 1.0 - wPop

        var predictedCycleLength = wPop * prior.cycleMean + wInd * individualMean
        val predictedVariance = wPop * (prior.cycleSD * prior.cycleSD) + wInd * (individualSD * individualSD)

        if (n >= MIN_CYCLES_FOR_TREND) {
            val recent = validCycles.takeLast(RECENT_CYCLE_COUNT).mapNotNull { it.cycleLength }
            if (recent.size >= MIN_RECENT_CYCLES_FOR_TREND) {
                val slope = linearRegressionSlope(recent)
                if (abs(slope) > TREND_THRESHOLD) {
                    predictedCycleLength += slope * TREND_ADJUSTMENT_FACTOR
                }
            }
        }

        predictedCycleLength = predictedCycleLength.coerceIn(MIN_PREDICTED_CYCLE_LENGTH, MAX_PREDICTED_CYCLE_LENGTH)

        val nextPeriodStart = currentCycleStartDate.plusDays(predictedCycleLength.roundToInt().toLong())

        val userLutealEstimate = if (n >= MIN_CYCLES_FOR_INDIVIDUAL_LUTEAL) {
            val estimatedFollicular = individualMean - prior.lutealMean
            val est = predictedCycleLength - estimatedFollicular
            est.coerceIn(7.0, 17.0)
        } else {
            prior.lutealMean
        }

        val ovulationDate = nextPeriodStart.minusDays(userLutealEstimate.roundToInt().toLong())
        val fertileWindowStart = ovulationDate.minusDays(PopulationPriors.FERTILE_WINDOW_START_BEFORE_OVULATION.toLong())
        val fertileWindowEnd = ovulationDate.plusDays(PopulationPriors.FERTILE_WINDOW_END_AFTER_OVULATION.toLong())
        val pmsStart = nextPeriodStart.minusDays(PopulationPriors.PMS_WINDOW_START_BEFORE_PERIOD.toLong())
        val pmsEnd = nextPeriodStart.minusDays(PopulationPriors.PMS_WINDOW_END_BEFORE_PERIOD.toLong())
        val periodDays = individualPeriodMean.roundToInt().toLong().coerceIn(1, 15)
        val periodEnd = nextPeriodStart.plusDays(periodDays - 1)

        val confidence = calculateConfidence(n, predictedVariance, sqrt(predictedVariance))
        val sqrtVar = ceil(sqrt(predictedVariance)).toLong().coerceAtLeast(1)
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
        numberOfFutureCycles: Int = DEFAULT_FUTURE_CYCLES
    ): List<Prediction> {
        if (cycles.isEmpty()) return emptyList()

        val safeFutureCycles = numberOfFutureCycles.coerceIn(1, 24)
        val lastCycle = cycles.maxByOrNull { it.startDate } ?: return emptyList()
        val predictions = mutableListOf<Prediction>()
        var currentStartDate = lastCycle.startDate

        for (i in 0 until safeFutureCycles) {
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
        confidence = confidence.coerceIn(MIN_CONFIDENCE, MAX_CONFIDENCE),
        lowerBound = lowerBound,
        upperBound = upperBound,
        algorithmVersion = PopulationPriors.ALGORITHM_VERSION
    )

    private fun adjustForSkips(cycleLength: Int, priorMean: Double): Double {
        if (priorMean <= 0) return cycleLength.toDouble()
        return if (cycleLength > priorMean * 1.8) {
            val divisor = (cycleLength / priorMean).roundToInt().coerceAtLeast(1)
            cycleLength.toDouble() / divisor
        } else {
            cycleLength.toDouble()
        }
    }

    private fun calculateConfidence(cyclesTracked: Int, variance: Double, sd: Double): Double {
        val cycleConf = min(0.9, 0.3 + cyclesTracked * 0.08)
        val variancePenalty = min(0.3, sd / 15.0)
        return (cycleConf - variancePenalty).coerceIn(MIN_CONFIDENCE, MAX_CONFIDENCE)
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
        return if (denominator > 0.0) numerator / denominator else 0.0
    }
}
