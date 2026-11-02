package com.unscroll.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unscroll.app.data.model.*
import com.unscroll.app.data.repository.SettingsRepository
import com.unscroll.app.data.repository.StatsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UnscrollUiState(
    val protectionMode: ProtectionMode = ProtectionMode.AUTOMATIC,
    val instagramConfig: AppProtectionConfig = AppProtectionConfig(TargetApp.INSTAGRAM),
    val youtubeConfig: AppProtectionConfig = AppProtectionConfig(TargetApp.YOUTUBE),
    val tiktokConfig: AppProtectionConfig = AppProtectionConfig(TargetApp.TIKTOK),
    val sleepConfig: SleepScheduleConfig = SleepScheduleConfig(),
    val focusSession: ActiveFocusSession = ActiveFocusSession(),
    val overview: ScreenTimeOverview = ScreenTimeOverview(),
    val appUsageList: List<DailyAppUsage> = emptyList(),
    val currentLanguage: String = "en",
    val isInterceptionOverlayActive: Boolean = false,
    val interceptedAppTitle: String = ""
)

class UnscrollViewModel(
    private val settingsRepository: SettingsRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnscrollUiState())
    val uiState: StateFlow<UnscrollUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Collect reactive repository state
        viewModelScope.launch {
            combine(
                settingsRepository.protectionMode,
                settingsRepository.instagramConfig,
                settingsRepository.youtubeConfig,
                settingsRepository.tiktokConfig,
                settingsRepository.sleepConfig,
                settingsRepository.language
            ) { mode, ig, yt, tt, sleep, lang ->
                _uiState.update { current ->
                    current.copy(
                        protectionMode = mode,
                        instagramConfig = ig,
                        youtubeConfig = yt,
                        tiktokConfig = tt,
                        sleepConfig = sleep,
                        currentLanguage = lang
                    )
                }
            }.collect()
        }

        refreshStats()
    }

    fun refreshStats() {
        val overview = statsRepository.getDailyOverview()
        val breakdown = statsRepository.getAppBreakdown()
        settingsRepository.refreshAllStatuses()
        _uiState.update {
            it.copy(
                overview = overview,
                appUsageList = breakdown
            )
        }
    }

    fun setProtectionMode(mode: ProtectionMode) {
        settingsRepository.updateProtectionMode(mode)
    }

    fun updateInstagramConfig(config: AppProtectionConfig) {
        settingsRepository.updateAppConfig(config)
    }

    fun updateYouTubeConfig(config: AppProtectionConfig) {
        settingsRepository.updateAppConfig(config)
    }

    fun updateTikTokConfig(config: AppProtectionConfig) {
        settingsRepository.updateAppConfig(config)
    }

    fun updateSleepSchedule(config: SleepScheduleConfig) {
        settingsRepository.updateSleepConfig(config)
    }

    fun setLanguage(langCode: String) {
        settingsRepository.setLanguage(langCode)
    }

    // --- Focus Mode Session Controls ---

    fun startFocusSession(durationMinutes: Int) {
        timerJob?.cancel()
        settingsRepository.setFocusDuration(durationMinutes)
        val totalSec = durationMinutes * 60
        _uiState.update {
            it.copy(
                focusSession = ActiveFocusSession(
                    isActive = true,
                    totalSeconds = totalSec,
                    remainingSeconds = totalSec,
                    startedAtTimestamp = System.currentTimeMillis(),
                    isPaused = false
                )
            )
        }

        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val currentRemaining = _uiState.value.focusSession.remainingSeconds
                if (!_uiState.value.focusSession.isPaused) {
                    if (currentRemaining <= 1) {
                        // Session completed
                        _uiState.update {
                            it.copy(
                                focusSession = it.focusSession.copy(
                                    isActive = false,
                                    remainingSeconds = 0
                                )
                            )
                        }
                        refreshStats()
                        break
                    } else {
                        _uiState.update {
                            it.copy(
                                focusSession = it.focusSession.copy(
                                    remainingSeconds = currentRemaining - 1
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun pauseFocusSession() {
        _uiState.update {
            it.copy(
                focusSession = it.focusSession.copy(
                    isPaused = !it.focusSession.isPaused
                )
            )
        }
    }

    fun cancelFocusSession() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                focusSession = ActiveFocusSession()
            )
        }
    }

    // --- Real Interception Handling ---
    fun onInterceptionTriggered(appName: String) {
        statsRepository.recordInterception()
        refreshStats()
        _uiState.update {
            it.copy(
                isInterceptionOverlayActive = true,
                interceptedAppTitle = appName
            )
        }
    }

    fun simulateInterception(appName: String) {
        onInterceptionTriggered(appName)
    }

    fun dismissInterception() {
        _uiState.update {
            it.copy(
                isInterceptionOverlayActive = false,
                interceptedAppTitle = ""
            )
        }
    }
}
