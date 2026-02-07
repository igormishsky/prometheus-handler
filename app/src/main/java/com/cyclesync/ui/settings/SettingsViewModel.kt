package com.cyclesync.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.domain.entity.AppMode
import com.cyclesync.domain.entity.Settings
import com.cyclesync.domain.repository.CycleRepository
import com.cyclesync.domain.repository.PredictionRepository
import com.cyclesync.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val mode: String = "Period Tracking",
    val typicalCycleLength: Int = 28,
    val typicalPeriodLength: Int = 5,
    val appIcon: String = "Default",
    val notificationPrivacy: String = "High",
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val cycleRepository: CycleRepository,
    private val predictionRepository: PredictionRepository
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
                    _state.value = SettingsState(
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
                val modes = AppMode.entries
                val currentIndex = modes.indexOf(settings.activeMode)
                val nextMode = modes[(currentIndex + 1) % modes.size]
                val updated = settings.copy(activeMode = nextMode)
                settingsRepository.updateSettings(updated)
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            cycleRepository.deleteAllCycles()
            predictionRepository.deleteAll()
        }
    }
}
