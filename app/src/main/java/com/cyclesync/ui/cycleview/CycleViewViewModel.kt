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
    val wellnessTip: String = "",
    val phaseSuperpower: String = "",
    val bodyInsight: String = "",
    val streakDays: Int = 0,
    val streakMessage: String = "",
    val error: String? = null
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
        val phaseSuperpowers = mapOf(
            CyclePhase.MENSTRUATION to listOf(
                "Your intuition is heightened right now \u2014 trust those gut feelings.",
                "This is your reset phase. The stillness you feel is your body recharging.",
                "Your brain hemispheres sync during menstruation \u2014 great for deep reflection.",
                "Rest isn\u2019t a weakness. Elite athletes periodize rest too. You\u2019re in your recovery arc."
            ),
            CyclePhase.FOLLICULAR to listOf(
                "Your brain is forming new neural connections faster right now. Learn something new!",
                "Estrogen is boosting your verbal fluency \u2014 perfect day for important conversations.",
                "Your pain tolerance is higher this phase. Push a little harder in that workout.",
                "Creativity peaks in the follicular phase. Start that project you\u2019ve been dreaming about."
            ),
            CyclePhase.OVULATION to listOf(
                "You\u2019re magnetically confident right now \u2014 this is your time to shine.",
                "Your communication skills are at their peak. Ask for that raise, have that talk.",
                "Research shows people perceive you as more attractive during ovulation. Own it.",
                "Your energy is at its absolute peak. You were literally built for this moment."
            ),
            CyclePhase.LUTEAL to listOf(
                "Your brain shifts to detail-oriented thinking now. Perfect for editing and refining.",
                "Progesterone makes you more discerning \u2014 trust your instinct to say no.",
                "This is your nesting phase. Organizing, planning, and completing feel natural.",
                "Your body is doing powerful hormonal work behind the scenes. Honor the process."
            ),
            CyclePhase.PMS to listOf(
                "Your inner critic is louder right now, but she\u2019s not telling the truth.",
                "The sensitivity you feel is a superpower \u2014 it deepens your empathy and awareness.",
                "This phase reveals what truly matters to you. Those feelings are data, not drama.",
                "You\u2019re about to emerge from this cycle renewed. The best is ahead."
            )
        )

        val bodyInsights = mapOf(
            CyclePhase.MENSTRUATION to listOf(
                "Your metabolism naturally slows \u2014 cravings for warm, comforting food are your body\u2019s wisdom.",
                "Iron levels drop during menstruation. Dark leafy greens and red meat help replenish.",
                "Prostaglandins cause cramps but also help your uterus shed efficiently. It\u2019s working as designed.",
                "Your immune system is slightly suppressed. Extra sleep isn\u2019t lazy \u2014 it\u2019s strategic."
            ),
            CyclePhase.FOLLICULAR to listOf(
                "Rising estrogen boosts serotonin and dopamine \u2014 that optimism you feel is biochemical.",
                "Your muscles recover faster in this phase. It\u2019s the ideal time for strength training.",
                "Skin tends to clear up as estrogen promotes collagen. Your glow is hormonal.",
                "Insulin sensitivity improves now \u2014 your body processes carbs more efficiently."
            ),
            CyclePhase.OVULATION to listOf(
                "Testosterone briefly spikes alongside estrogen, boosting libido and assertiveness.",
                "Your cervical mucus changes to an egg-white consistency \u2014 a key fertility sign.",
                "Body temperature rises slightly after ovulation (0.2\u20130.5\u00b0C) \u2014 confirming the shift.",
                "Estrogen peaks make your skin glow and your features appear subtly more symmetrical."
            ),
            CyclePhase.LUTEAL to listOf(
                "Progesterone raises your resting body temperature. Feeling warmer than usual is normal.",
                "Your metabolism speeds up 2.5\u201311% \u2014 you genuinely need ~100\u2013300 extra calories.",
                "Water retention causes bloating. Drinking more water paradoxically reduces it.",
                "Progesterone has a mild sedative effect. Needing more sleep is your body being smart."
            ),
            CyclePhase.PMS to listOf(
                "Dropping serotonin explains carb cravings \u2014 complex carbs genuinely help boost mood.",
                "Magnesium levels dip before your period. Dark chocolate is actually medicinal right now.",
                "Breast tenderness is caused by fluid retention from progesterone. It will pass.",
                "Emotional sensitivity increases as estrogen and progesterone both drop rapidly."
            )
        )

        fun getStreakMessage(days: Int): String = when {
            days == 0 -> "Start tracking today to build your streak!"
            days == 1 -> "Day 1 \u2014 every journey starts with a single step."
            days < 7 -> "$days-day streak! You\u2019re building a powerful habit."
            days < 14 -> "$days days strong! Your predictions are getting more accurate."
            days < 30 -> "$days-day streak! You\u2019re uncovering patterns most people never see."
            days < 60 -> "$days days! Your cycle data is revealing deep insights about your body."
            days < 90 -> "$days-day streak! You know your body better than most people ever will."
            else -> "$days days! You\u2019re a cycle-tracking powerhouse."
        }

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
            try {
                combine(
                    cycleRepository.getCurrentCycleFlow(),
                    predictionRepository.getActivePredictions(),
                    settingsRepository.getSettings()
                ) { currentCycle, predictions, settings ->
                    buildState(currentCycle, predictions, settings)
                }.collect { newState ->
                    _state.value = newState
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load cycle data"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
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

    private fun getPhaseSuperpowerForToday(phase: CyclePhase): String {
        val powers = phaseSuperpowers[phase] ?: phaseSuperpowers[CyclePhase.FOLLICULAR]!!
        val dayOfYear = LocalDate.now().dayOfYear
        return powers[dayOfYear % powers.size]
    }

    private fun getBodyInsightForToday(phase: CyclePhase): String {
        val insights = bodyInsights[phase] ?: bodyInsights[CyclePhase.FOLLICULAR]!!
        val dayOfYear = LocalDate.now().dayOfYear
        // Offset by 7 so it doesn't rotate on the same schedule as superpowers
        return insights[(dayOfYear + 7) % insights.size]
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
                wellnessTip = getWellnessTipForPhase(CyclePhase.FOLLICULAR),
                phaseSuperpower = getPhaseSuperpowerForToday(CyclePhase.FOLLICULAR),
                bodyInsight = getBodyInsightForToday(CyclePhase.FOLLICULAR),
                streakDays = 0,
                streakMessage = getStreakMessage(0)
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

        val streakDays = currentCycle.startDate?.let {
            ChronoUnit.DAYS.between(it, today).toInt().coerceAtLeast(0)
        } ?: 0

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
            wellnessTip = getWellnessTipForPhase(phase),
            phaseSuperpower = getPhaseSuperpowerForToday(phase),
            bodyInsight = getBodyInsightForToday(phase),
            streakDays = streakDays,
            streakMessage = getStreakMessage(streakDays)
        )
    }
}
