package com.cyclesync.ui.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.utils.DateUtils
import com.cyclesync.core.utils.UuidGenerator
import com.cyclesync.domain.entity.Cycle
import com.cyclesync.domain.entity.DailyLog
import com.cyclesync.domain.entity.SkipReason
import com.cyclesync.domain.entity.TrackingCategory
import com.cyclesync.domain.entity.TrackingEntry
import com.cyclesync.domain.prediction.PredictionEngine
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.DailyLogRepository
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class TrackingState(
    val date: LocalDate = LocalDate.now(),
    val existingLog: DailyLog? = null,
    val entries: MutableMap<String, TrackingEntry> = mutableMapOf(),
    val notes: String = "",
    val isPeriodDay: Boolean = false,
    val flowIntensity: String? = null,
    val isIrregular: Boolean = false,
    val skipReason: SkipReason? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dailyLogRepository: DailyLogRepository,
    private val cycleRepository: CycleRepository,
    private val settingsRepository: SettingsRepository,
    private val predictionRepository: PredictionRepository,
    private val predictionEngine: PredictionEngine
) : ViewModel() {

    private val dateStr: String = savedStateHandle["date"] ?: DateUtils.toIsoString(LocalDate.now())
    private val _state = MutableStateFlow(TrackingState(date = DateUtils.fromIsoString(dateStr)))
    val state: StateFlow<TrackingState> = _state.asStateFlow()

    init {
        loadExistingLog()
    }

    private fun loadExistingLog() {
        viewModelScope.launch {
            try {
                val date = DateUtils.fromIsoString(dateStr)
                val existingLog = dailyLogRepository.getByDate(date)
                val currentCycle = cycleRepository.getCurrentCycle()
                val isPeriod = currentCycle?.isDateInPeriod(date) ?: false

                val flow = existingLog?.entries?.find {
                    it.category == TrackingCategory.BLEEDING && it.subcategory.startsWith("flow_")
                }?.subcategory?.removePrefix("flow_")

                val entriesMap = mutableMapOf<String, TrackingEntry>()
                existingLog?.entries?.forEach { entry ->
                    entriesMap[entry.key] = entry
                }

                _state.value = _state.value.copy(
                    existingLog = existingLog,
                    isPeriodDay = isPeriod,
                    flowIntensity = flow,
                    notes = existingLog?.notes ?: "",
                    entries = entriesMap,
                    isLoading = false
                )
            } catch (_: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = "Failed to load tracking data")
            }
        }
    }

    fun togglePeriod(isOn: Boolean) {
        _state.value = _state.value.copy(isPeriodDay = isOn)
        if (isOn && _state.value.flowIntensity == null) {
            _state.value = _state.value.copy(flowIntensity = "medium")
        }
    }

    fun setFlowIntensity(intensity: String) {
        _state.value = _state.value.copy(flowIntensity = intensity)
    }

    fun toggleEntry(category: TrackingCategory, subcategory: String, intensity: Int? = null) {
        val key = "${category.name}_$subcategory"
        val current = _state.value.entries.toMutableMap()
        if (current.containsKey(key)) {
            current.remove(key)
        } else {
            current[key] = TrackingEntry(
                id = UuidGenerator.generate(),
                dailyLogId = "",
                category = category,
                subcategory = subcategory,
                intensity = intensity
            )
        }
        _state.value = _state.value.copy(entries = current)
    }

    fun setNotes(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }

    fun toggleIrregular(isIrregular: Boolean) {
        _state.value = _state.value.copy(
            isIrregular = isIrregular,
            skipReason = if (!isIrregular) null else _state.value.skipReason
        )
    }

    fun setSkipReason(reason: SkipReason?) {
        _state.value = _state.value.copy(skipReason = reason)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun save() {
        if (_state.value.isSaving) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)
            try {
                val date = _state.value.date
                val logId = _state.value.existingLog?.id ?: UuidGenerator.generate()
                val currentCycle = cycleRepository.getCurrentCycle()

                if (_state.value.isPeriodDay && currentCycle == null) {
                    val cycleNum = cycleRepository.getCycleCount() + 1
                    val previousCycle = cycleRepository.getLastCycle()

                    previousCycle?.let { prev ->
                        cycleRepository.updateCycle(
                            prev.copy(
                                endDate = date.minusDays(1),
                                cycleLength = DateUtils.daysBetween(prev.startDate, date).toInt()
                            )
                        )
                    }

                    val newCycle = Cycle(
                        id = UuidGenerator.generate(),
                        cycleNumber = cycleNum,
                        startDate = date,
                        periodStartDate = date,
                        isExcluded = _state.value.isIrregular,
                        skipReason = _state.value.skipReason
                    )
                    cycleRepository.insertCycle(newCycle)
                    regeneratePredictions()
                }

                val entries = _state.value.entries.values.map { entry ->
                    entry.copy(dailyLogId = logId)
                }.toMutableList()

                if (_state.value.isPeriodDay && _state.value.flowIntensity != null) {
                    entries.add(
                        TrackingEntry(
                            id = UuidGenerator.generate(),
                            dailyLogId = logId,
                            category = TrackingCategory.BLEEDING,
                            subcategory = "flow_${_state.value.flowIntensity}"
                        )
                    )
                }

                val log = DailyLog(
                    id = logId,
                    date = date,
                    cycleId = currentCycle?.id ?: cycleRepository.getCurrentCycle()?.id,
                    notes = _state.value.notes.takeIf { it.isNotBlank() }
                )

                dailyLogRepository.insertOrUpdate(log)
                dailyLogRepository.saveTrackingEntries(logId, entries)

                _state.value = _state.value.copy(isSaved = true, isSaving = false)
            } catch (_: Exception) {
                _state.value = _state.value.copy(isSaving = false, error = "Failed to save tracking data")
            }
        }
    }

    private suspend fun regeneratePredictions() {
        val settings = settingsRepository.getSettingsOnce()
        val cycles = cycleRepository.getAllCyclesOnce()
        val age = settings?.currentAge
        val bmi = settings?.bmiCategoryName

        val predictions = predictionEngine.generatePredictions(cycles, age, bmi)
        predictionRepository.savePredictions(predictions)
    }
}
