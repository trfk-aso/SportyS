package com.example.sportys.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sportys.repository.SettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class SplashViewModel(
    private val settings: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState

    init {
        viewModelScope.launch {
            val first = settings.isFirstLaunch()
            _uiState.value = SplashUiState(first)
            if (first) settings.setFirstLaunch(false)
        }
    }

    fun markLaunched() {
        viewModelScope.launch {
            settings.setFirstLaunch(false)
            _uiState.value = SplashUiState(false)
        }
    }

    fun splashDelay(seconds: Long = 1L): Flow<Unit> = flow {
        delay(seconds * 1000)
        emit(Unit)
    }
}

data class SplashUiState(
    val isFirstLaunch: Boolean = true
)