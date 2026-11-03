package com.unscroll.app.data.repository

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.unscroll.app.data.model.*
import com.unscroll.app.service.UnscrollAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository responsible for persistent storage of user settings and
 * verifying actual system service running status.
 */
class SettingsRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "unscroll_preferences",
        Context.MODE_PRIVATE
    )

    private val _protectionMode = MutableStateFlow(
        ProtectionMode.valueOf(prefs.getString(KEY_PROTECTION_MODE, ProtectionMode.AUTOMATIC.name) ?: ProtectionMode.AUTOMATIC.name)
    )
    val protectionMode: StateFlow<ProtectionMode> = _protectionMode.asStateFlow()

    private val _instagramConfig = MutableStateFlow(
        createAppConfig(
            TargetApp.INSTAGRAM,
            KEY_IG_ENABLED,
            KEY_IG_REELS_ONLY,
            KEY_IG_LIMIT,
            KEY_IG_PAUSE,
            KEY_IG_EXPLORE
        )
    )
    val instagramConfig: StateFlow<AppProtectionConfig> = _instagramConfig.asStateFlow()

    private val _youtubeConfig = MutableStateFlow(
        createAppConfig(
            TargetApp.YOUTUBE,
            KEY_YT_ENABLED,
            KEY_YT_SHORTS_ONLY,
            KEY_YT_LIMIT,
            KEY_YT_PAUSE,
            null
        )
    )
    val youtubeConfig: StateFlow<AppProtectionConfig> = _youtubeConfig.asStateFlow()

    private val _tiktokConfig = MutableStateFlow(
        createAppConfig(
            TargetApp.TIKTOK,
            KEY_TT_ENABLED,
            null,
            KEY_TT_LIMIT,
            KEY_TT_PAUSE,
            KEY_TT_EXPLORE
        )
    )
    val tiktokConfig: StateFlow<AppProtectionConfig> = _tiktokConfig.asStateFlow()

    private val _sleepConfig = MutableStateFlow(
        SleepScheduleConfig(
            isEnabled = prefs.getBoolean(KEY_SLEEP_ENABLED, false),
            bedtimeHour = prefs.getInt(KEY_SLEEP_BED_H, 23),
            bedtimeMinute = prefs.getInt(KEY_SLEEP_BED_M, 0),
            wakeHour = prefs.getInt(KEY_SLEEP_WAKE_H, 7),
            wakeMinute = prefs.getInt(KEY_SLEEP_WAKE_M, 0),
            windDownLeadMinutes = prefs.getInt(KEY_SLEEP_WIND_DOWN, 30),
            blockAllTargetApps = prefs.getBoolean(KEY_SLEEP_BLOCK_ALL, true),
            enableGrayscaleBedtime = prefs.getBoolean(KEY_SLEEP_GRAYSCALE, true)
        )
    )
    val sleepConfig: StateFlow<SleepScheduleConfig> = _sleepConfig.asStateFlow()

    private val _focusDurationMinutes = MutableStateFlow(prefs.getInt(KEY_FOCUS_DURATION, 25))
    val focusDurationMinutes: StateFlow<Int> = _focusDurationMinutes.asStateFlow()

    private val _language = MutableStateFlow(prefs.getString(KEY_LANGUAGE, "en") ?: "en")
    val language: StateFlow<String> = _language.asStateFlow()

    private fun createAppConfig(
        app: TargetApp,
        enabledKey: String,
        reelsOnlyKey: String?,
        limitKey: String,
        pauseKey: String,
        exploreKey: String?
    ): AppProtectionConfig {
        val isEnabled = prefs.getBoolean(enabledKey, false)
        val shortFormOnly = if (reelsOnlyKey != null) prefs.getBoolean(reelsOnlyKey, true) else false
        val limit = prefs.getInt(limitKey, app.defaultDailyLimitMinutes)
        val pause = prefs.getInt(pauseKey, 10)
        val explore = if (exploreKey != null) prefs.getBoolean(exploreKey, true) else false

        val status = resolveStatus(isEnabled)

        return AppProtectionConfig(
            app = app,
            isProtectionEnabled = isEnabled,
            blockShortFormOnly = shortFormOnly,
            dailyLimitMinutes = limit,
            intentionalPauseSeconds = pause,
            blockExplorePage = explore,
            status = status
        )
    }

    /**
     * Resolves the verified status of protection.
     * Never labels as ACTIVE unless the Accessibility service is actually running.
     */
    fun resolveStatus(isAppEnabledByUser: Boolean): ServiceStatus {
        if (!isAppEnabledByUser) {
            return ServiceStatus.NOT_CONFIGURED
        }

        // Check if Accessibility Service is enabled in Android System Settings
        val isAccessibilityEnabledInSystem = isAccessibilityServiceEnabled()
        if (!isAccessibilityEnabledInSystem) {
            return ServiceStatus.PERMISSION_REQUIRED
        }

        // Check if Overlay permission is granted (required for intercept barrier on Android 6.0+)
        val canDrawOverlays = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
        if (!canDrawOverlays) {
            return ServiceStatus.PERMISSION_REQUIRED
        }

        // Check if service process is currently active
        return if (UnscrollAccessibilityService.isServiceRunning) {
            ServiceStatus.ACTIVE
        } else {
            ServiceStatus.ERROR
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceName = UnscrollAccessibilityService::class.java.name
        return enabledServices.any {
            val component = it.resolveInfo?.serviceInfo
            component != null && component.packageName == context.packageName && component.name == expectedServiceName
        }
    }

    fun updateProtectionMode(mode: ProtectionMode) {
        prefs.edit().putString(KEY_PROTECTION_MODE, mode.name).apply()
        _protectionMode.value = mode
    }

    fun updateAppConfig(config: AppProtectionConfig) {
        val verifiedConfig = config.copy(status = resolveStatus(config.isProtectionEnabled))
        when (config.app) {
            TargetApp.INSTAGRAM -> {
                prefs.edit()
                    .putBoolean(KEY_IG_ENABLED, verifiedConfig.isProtectionEnabled)
                    .putBoolean(KEY_IG_REELS_ONLY, verifiedConfig.blockShortFormOnly)
                    .putInt(KEY_IG_LIMIT, verifiedConfig.dailyLimitMinutes)
                    .putInt(KEY_IG_PAUSE, verifiedConfig.intentionalPauseSeconds)
                    .putBoolean(KEY_IG_EXPLORE, verifiedConfig.blockExplorePage)
                    .apply()
                _instagramConfig.value = verifiedConfig
            }
            TargetApp.YOUTUBE -> {
                prefs.edit()
                    .putBoolean(KEY_YT_ENABLED, verifiedConfig.isProtectionEnabled)
                    .putBoolean(KEY_YT_SHORTS_ONLY, verifiedConfig.blockShortFormOnly)
                    .putInt(KEY_YT_LIMIT, verifiedConfig.dailyLimitMinutes)
                    .putInt(KEY_YT_PAUSE, verifiedConfig.intentionalPauseSeconds)
                    .apply()
                _youtubeConfig.value = verifiedConfig
            }
            TargetApp.TIKTOK -> {
                prefs.edit()
                    .putBoolean(KEY_TT_ENABLED, verifiedConfig.isProtectionEnabled)
                    .putInt(KEY_TT_LIMIT, verifiedConfig.dailyLimitMinutes)
                    .putInt(KEY_TT_PAUSE, verifiedConfig.intentionalPauseSeconds)
                    .putBoolean(KEY_TT_EXPLORE, verifiedConfig.blockExplorePage)
                    .apply()
                _tiktokConfig.value = verifiedConfig
            }
        }
    }

    fun updateSleepConfig(config: SleepScheduleConfig) {
        prefs.edit()
            .putBoolean(KEY_SLEEP_ENABLED, config.isEnabled)
            .putInt(KEY_SLEEP_BED_H, config.bedtimeHour)
            .putInt(KEY_SLEEP_BED_M, config.bedtimeMinute)
            .putInt(KEY_SLEEP_WAKE_H, config.wakeHour)
            .putInt(KEY_SLEEP_WAKE_M, config.wakeMinute)
            .putInt(KEY_SLEEP_WIND_DOWN, config.windDownLeadMinutes)
            .putBoolean(KEY_SLEEP_BLOCK_ALL, config.blockAllTargetApps)
            .putBoolean(KEY_SLEEP_GRAYSCALE, config.enableGrayscaleBedtime)
            .apply()
        _sleepConfig.value = config
    }

    fun setFocusDuration(minutes: Int) {
        prefs.edit().putInt(KEY_FOCUS_DURATION, minutes).apply()
        _focusDurationMinutes.value = minutes
    }

    fun setLanguage(langCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, langCode).apply()
        _language.value = langCode
    }

    fun refreshAllStatuses() {
        _instagramConfig.value = _instagramConfig.value.copy(status = resolveStatus(_instagramConfig.value.isProtectionEnabled))
        _youtubeConfig.value = _youtubeConfig.value.copy(status = resolveStatus(_youtubeConfig.value.isProtectionEnabled))
        _tiktokConfig.value = _tiktokConfig.value.copy(status = resolveStatus(_tiktokConfig.value.isProtectionEnabled))
    }

    /**
     * Reload persisted protection choices. The Accessibility service owns a separate
     * repository instance from the Activity, so its StateFlows otherwise become stale.
     */
    fun refreshProtectionConfigs() {
        _instagramConfig.value = createAppConfig(
            TargetApp.INSTAGRAM, KEY_IG_ENABLED, KEY_IG_REELS_ONLY,
            KEY_IG_LIMIT, KEY_IG_PAUSE, KEY_IG_EXPLORE
        )
        _youtubeConfig.value = createAppConfig(
            TargetApp.YOUTUBE, KEY_YT_ENABLED, KEY_YT_SHORTS_ONLY,
            KEY_YT_LIMIT, KEY_YT_PAUSE, null
        )
    }

    companion object {
        private const val KEY_PROTECTION_MODE = "key_protection_mode"
        private const val KEY_IG_ENABLED = "key_ig_enabled"
        private const val KEY_IG_REELS_ONLY = "key_ig_reels_only"
        private const val KEY_IG_LIMIT = "key_ig_limit"
        private const val KEY_IG_PAUSE = "key_ig_pause"
        private const val KEY_IG_EXPLORE = "key_ig_explore"

        private const val KEY_YT_ENABLED = "key_yt_enabled"
        private const val KEY_YT_SHORTS_ONLY = "key_yt_shorts_only"
        private const val KEY_YT_LIMIT = "key_yt_limit"
        private const val KEY_YT_PAUSE = "key_yt_pause"

        private const val KEY_TT_ENABLED = "key_tt_enabled"
        private const val KEY_TT_LIMIT = "key_tt_limit"
        private const val KEY_TT_PAUSE = "key_tt_pause"
        private const val KEY_TT_EXPLORE = "key_tt_explore"

        private const val KEY_SLEEP_ENABLED = "key_sleep_enabled"
        private const val KEY_SLEEP_BED_H = "key_sleep_bed_h"
        private const val KEY_SLEEP_BED_M = "key_sleep_bed_m"
        private const val KEY_SLEEP_WAKE_H = "key_sleep_wake_h"
        private const val KEY_SLEEP_WAKE_M = "key_sleep_wake_m"
        private const val KEY_SLEEP_WIND_DOWN = "key_sleep_wind_down"
        private const val KEY_SLEEP_BLOCK_ALL = "key_sleep_block_all"
        private const val KEY_SLEEP_GRAYSCALE = "key_sleep_grayscale"

        private const val KEY_FOCUS_DURATION = "key_focus_duration"
        private const val KEY_LANGUAGE = "key_language"
    }
}
