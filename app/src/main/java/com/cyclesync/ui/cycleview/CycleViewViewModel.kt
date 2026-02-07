package com.cyclesync.ui.cycleview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.constants.PopulationPriors
import com.cyclesync.core.utils.CycleCalculator
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.CyclePhase
import com.cyclesync.domain.entity.Prediction
import com.cyclesync.domain.entity.PredictionType
import com.cyclesync.domain.entity.Settings
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class CycleViewState(
    val currentCycleDay: Int = 0,
    val predictedCycleLength: Int = 28,
    val periodLength: Int = 5,
    val currentPhase: CyclePhase = CyclePhase.FOLLICULAR,
    val daysUntilNextPeriod: Int? = null,
    val confidence: Double = 0.0,
    val phaseName: String = "Follicular",
    val cycleStartDate: LocalDate? = null,
    val mode: String = "Period Tracking",
    val isLoading: Boolean = true,
    val hasCycles: Boolean = false,
    val nextPeriodDate: LocalDate? = null,
    val ovulationDate: LocalDate? = null,
    val fertileWindowStart: LocalDate? = null,
    val fertileWindowEnd: LocalDate? = null,
    val pmsStart: LocalDate? = null,
    val dailyAffirmation: String = "",
    val wellnessTip: String = ""
)

@HiltViewModel
class CycleViewViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val predictionRepository: PredictionRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CycleViewState())
    val state: StateFlow<CycleViewState> = _state.asStateFlow()

    companion object {
        val affirmations = listOf(
            "You are strong, capable, and worthy of love.",
            "Your body is amazing and deserves kindness.",
            "Today is a new opportunity to nurture yourself.",
            "You are enough, just as you are right now.",
            "Trust your body \u2014 it knows what it needs.",
            "You deserve rest without guilt.",
            "Your feelings are valid and important.",
            "Be gentle with yourself today.",
            "You are resilient and full of inner strength.",
            "Celebrate the small victories today.",
            "You radiate beauty from the inside out.",
            "Prioritize your well-being \u2014 you matter.",
            "You are worthy of taking up space.",
            "Honor your body\u2019s rhythm and pace.",
            "Every phase of your cycle is a superpower.",
            "Your self-care is not selfish \u2014 it\u2019s essential.",
            "You bring light to the world around you.",
            "Listen to your body; it speaks wisdom.",
            "You are growing stronger every single day.",
            "Embrace where you are on your journey."
        )

        val wellnessTips = mapOf(
            CyclePhase.MENSTRUATION to listOf(
                "Warm compresses can help ease cramps. Try a heating pad on your lower abdomen.",
                "Iron-rich foods like spinach and lentils help replenish what your body loses.",
                "Gentle yoga and stretching can relieve tension and improve blood flow.",
                "Stay hydrated \u2014 warm herbal teas like chamomile and ginger are soothing.",
                "Rest is productive. Give yourself permission to slow down."
            ),
            CyclePhase.FOLLICULAR to listOf(
                "Your energy is rising! Great time to start new projects or try a new workout.",
                "Estrogen is climbing \u2014 you may feel more creative and social right now.",
                "Light, fresh meals with plenty of vegetables support your rising energy.",
                "This is a great time for strength training as your body recovers faster.",
                "Your skin may be at its clearest \u2014 perfect time for a gentle exfoliation."
            ),
            CyclePhase.OVULATION to listOf(
                "You\u2019re at peak energy! Channel it into activities you love.",
                "Communication skills peak now \u2014 great for important conversations.",
                "High-intensity workouts feel easier during this phase.",
                "Stay hydrated and eat antioxidant-rich foods like berries and leafy greens.",
                "Your confidence may be higher \u2014 embrace it!"
            ),
            CyclePhase.LUTEAL to listOf(
                "Progesterone rises now. Complex carbs and magnesium-rich foods can help.",
                "Gentle movement like walking or swimming is ideal as energy shifts.",
                "Dark chocolate (in moderation) can satisfy cravings and boost mood.",
                "Journaling or meditation can help process emotions during this phase.",
                "Prioritize sleep \u2014 your body is doing important work behind the scenes."
            ),
            CyclePhase.PMS to listOf(
                "Be extra kind to yourself. Cravings and mood shifts are normal.",
                "Calcium and vitamin B6 may help reduce PMS symptoms.",
                "A warm bath with Epsom salts can ease tension and soothe muscles.",
                "Limit caffeine and salt to reduce bloating and irritability.",
                "Cozy self-care rituals like face masks and candles can lift your spirits."
            )
        )
    }

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                cycleRepository.getCurrentCycleFlow(),
                predictionRepository.getActivePredictions(),
                settingsRepository.getSettings()
            ) { currentCycle, predictions, settings ->
                buildState(currentCycle, predictions, settings)
            }.collect { newState ->
                _state.value = newState
            }
        }
    }

    private fun getAffirmationForToday(): String {
        val dayOfYear = LocalDate.now().dayOfYear
        return affirmations[dayOfYear % affirmations.size]
    }

    private fun getWellnessTipForPhase(phase: CyclePhase): String {
        val tips = wellnessTips[phase] ?: wellnessTips[CyclePhase.FOLLICULAR]!!
        val dayOfYear = LocalDate.now().dayOfYear
        return tips[dayOfYear % tips.size]
    }

    private fun buildState(
        currentCycle: Cycle?,
        predictions: List<Prediction>,
        settings: Settings?
    ): CycleViewState {
        val today = LocalDate.now()
        val typicalCycleLength = settings?.typicalCycleLength ?: 28
        val typicalPeriodLength = settings?.typicalPeriodLength ?: 5

        if (currentCycle == null) {
            return CycleViewState(
                isLoading = false,
                hasCycles = false,
                predictedCycleLength = typicalCycleLength,
                periodLength = typicalPeriodLength,
                mode = settings?.activeMode?.displayName ?: "Period Tracking",
                dailyAffirmation = getAffirmationForToday(),
                wellnessTip = getWellnessTipForPhase(CyclePhase.FOLLICULAR)
            )
        }

        val cycleDay = CycleCalculator.getCurrentCycleDay(currentCycle.startDate, today)
        val nextPeriodPrediction = predictions
            .filter { it.type == PredictionType.PERIOD_START && it.predictedDate.isAfter(today) }
            .minByOrNull { it.predictedDate }

        val ovulationPrediction = predictions
            .filter { it.type == PredictionType.OVULATION && it.predictedDate.isAfter(currentCycle.startDate) }
            .minByOrNull { it.predictedDate }

        val fertileStart = predictions
            .filter { it.type == PredictionType.FERTILE_START && it.predictedDate.isAfter(currentCycle.startDate) }
            .minByOrNull { it.predictedDate }

        val fertileEnd = predictions
            .filter { it.type == PredictionType.FERTILE_END && it.predictedDate.isAfter(currentCycle.startDate) }
            .minByOrNull { it.predictedDate }

        val pmsStart = predictions
            .filter { it.type == PredictionType.PMS_START && it.predictedDate.isAfter(currentCycle.startDate) }
            .minByOrNull { it.predictedDate }

        val predictedLength = nextPeriodPrediction?.let {
            ChronoUnit.DAYS.between(currentCycle.startDate, it.predictedDate).toInt()
        } ?: typicalCycleLength

        val periodLen = currentCycle.periodLength ?: typicalPeriodLength

        val phase = CycleCalculator.determineCyclePhase(cycleDay, predictedLength, periodLen)

        val daysUntil = nextPeriodPrediction?.let {
            ChronoUnit.DAYS.between(today, it.predictedDate).toInt()
        }

        return CycleViewState(
            currentCycleDay = cycleDay,
            predictedCycleLength = predictedLength,
            periodLength = periodLen,
            currentPhase = phase,
            daysUntilNextPeriod = daysUntil,
            confidence = nextPeriodPrediction?.confidence ?: 0.3,
            phaseName = phase.displayName,
            cycleStartDate = currentCycle.startDate,
            mode = settings?.activeMode?.displayName ?: "Period Tracking",
            isLoading = false,
            hasCycles = true,
            nextPeriodDate = nextPeriodPrediction?.predictedDate,
            ovulationDate = ovulationPrediction?.predictedDate,
            fertileWindowStart = fertileStart?.predictedDate,
            fertileWindowEnd = fertileEnd?.predictedDate,
            pmsStart = pmsStart?.predictedDate,
            dailyAffirmation = getAffirmationForToday(),
            wellnessTip = getWellnessTipForPhase(phase)
        )
    }
}
