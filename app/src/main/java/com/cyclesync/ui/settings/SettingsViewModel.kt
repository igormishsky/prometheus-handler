package com.cyclesync.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.core.notifications.ReminderManager
import com.cyclesync.core.utils.CsvExporter
import com.cyclesync.domain.entity.AppMode
import com.cyclesync.domain.entity.Settings
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

data class SettingsState(
    val mode: String = "Period Tracking",
    val typicalCycleLength: Int = 28,
    val typicalPeriodLength: Int = 5,
    val appIcon: String = "Default",
    val notificationPrivacy: String = "High",
    val remindersEnabled: Boolean = false,
    val dailyLogReminder: Boolean = false,
    val isLoading: Boolean = true,
    val exportUri: Uri? = null,
    val exportReady: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val cycleRepository: CycleRepository,
    private val dailyLogRepository: DailyLogRepository,
    private val predictionRepository: PredictionRepository,
    private val reminderManager: ReminderManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private var currentSettings: Settings? = null

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                currentSettings = settings
                settings?.let {
                    _state.value = _state.value.copy(
                        mode = it.activeMode.displayName,
                        typicalCycleLength = it.typicalCycleLength,
                        typicalPeriodLength = it.typicalPeriodLength,
                        appIcon = it.appIcon.replaceFirstChar { c -> c.uppercase() },
                        notificationPrivacy = it.notificationPrivacy.displayName,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun cycleMode() {
        viewModelScope.launch {
            currentSettings?.let { settings ->
                val nextMode = AppMode.next(settings.activeMode)
                val updated = settings.copy(activeMode = nextMode)
                settingsRepository.updateSettings(updated)
            }
        }
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                val cycles = cycleRepository.getAllCyclesOnce()
                val firstCycleDate = cycles.minOfOrNull { it.startDate } ?: LocalDate.now().minusYears(1)
                val dailyLogs = dailyLogRepository.getByDateRange(firstCycleDate, LocalDate.now())

                val uri = CsvExporter.exportCyclesToCsv(context, cycles, dailyLogs)
                if (uri != null) {
                    _state.value = _state.value.copy(exportUri = uri, exportReady = true, error = null)
                } else {
                    _state.value = _state.value.copy(error = "Failed to export data")
                }
            } catch (_: Exception) {
                _state.value = _state.value.copy(error = "Failed to export data")
            }
        }
    }

    fun clearExportState() {
        _state.value = _state.value.copy(exportUri = null, exportReady = false)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun toggleReminders(context: Context, enabled: Boolean) {
        _state.value = _state.value.copy(remindersEnabled = enabled)
        if (enabled) {
            reminderManager.createNotificationChannel(context)
            currentSettings?.let { settings ->
                reminderManager.scheduleDailyLogReminder(context, settings.notificationPrivacy)
            }
        } else {
            reminderManager.cancelAllReminders(context)
        }
    }

    fun toggleDailyLogReminder(context: Context, enabled: Boolean) {
        _state.value = _state.value.copy(dailyLogReminder = enabled)
        if (enabled) {
            reminderManager.createNotificationChannel(context)
            currentSettings?.let { settings ->
                reminderManager.scheduleDailyLogReminder(context, settings.notificationPrivacy)
            }
        } else {
            reminderManager.cancelAllReminders(context)
            if (_state.value.remindersEnabled) {
                reminderManager.createNotificationChannel(context)
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            try {
                cycleRepository.deleteAllCycles()
                predictionRepository.deleteAll()
            } catch (_: Exception) {
                _state.value = _state.value.copy(error = "Failed to delete data")
            }
        }
    }
}
