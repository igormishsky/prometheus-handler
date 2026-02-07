package com.cyclesync.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyclesync.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

data class LoginState(
    val pin: String = "",
    val isSettingUp: Boolean = false,
    val confirmPin: String = "",
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    val hasPinSet: StateFlow<Boolean> = settingsRepository.getSettings()
        .map { it?.pinHash != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onPinDigitEntered(digit: String) {
        val current = _state.value
        if (current.isSettingUp && current.pin.length == 4) {
            // Entering confirmation PIN
            if (current.confirmPin.length < 4) {
                val newConfirm = current.confirmPin + digit
                _state.value = current.copy(confirmPin = newConfirm, error = null)
                if (newConfirm.length == 4) {
                    confirmSetup(current.pin, newConfirm)
                }
            }
        } else if (current.pin.length < 4) {
            val newPin = current.pin + digit
            _state.value = current.copy(pin = newPin, error = null)
            if (newPin.length == 4 && !current.isSettingUp) {
                verifyPin(newPin)
            }
        }
    }

    fun onBackspace() {
        val current = _state.value
        if (current.isSettingUp && current.pin.length == 4 && current.confirmPin.isNotEmpty()) {
            _state.value = current.copy(confirmPin = current.confirmPin.dropLast(1), error = null)
        } else if (current.pin.isNotEmpty()) {
            _state.value = current.copy(pin = current.pin.dropLast(1), error = null)
        }
    }

    fun startSetup() {
        _state.value = LoginState(isSettingUp = true)
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            val settings = settingsRepository.getSettingsOnce()
            val hash = hashPin(pin)
            if (settings?.pinHash == hash) {
                _state.value = _state.value.copy(isAuthenticated = true)
            } else {
                _state.value = _state.value.copy(pin = "", error = "Incorrect PIN. Try again.")
            }
        }
    }

    private fun confirmSetup(pin: String, confirm: String) {
        if (pin != confirm) {
            _state.value = LoginState(
                isSettingUp = true,
                error = "PINs don't match. Try again."
            )
            return
        }
        viewModelScope.launch {
            val hash = hashPin(pin)
            val settings = settingsRepository.getSettingsOnce()
            settings?.let {
                settingsRepository.updateSettings(it.copy(pinHash = hash))
            }
            _state.value = _state.value.copy(isAuthenticated = true)
        }
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
