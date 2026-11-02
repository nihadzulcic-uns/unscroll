package com.unscroll.app.data.model

/**
 * Operating mode for doomscroll detection and blocking.
 */
enum class ProtectionMode {
    /**
     * Automatically senses Reels, Shorts, and TikTok video elements in the UI tree
     * using AccessibilityService and brings up an intentional pause barrier.
     */
    AUTOMATIC,

    /**
     * Applies manual time limits and schedules (e.g. max 15m/day) before blocking.
     */
    MANUAL,

    /**
     * Complete block during active focus or sleep hours without bypass options.
     */
    STRICT
}

/**
 * Protection Service verification status.
 * Never labeled as ACTIVE unless verified running on the device.
 */
enum class ServiceStatus(val displayName: String) {
    NOT_CONFIGURED("Not configured"),
    PERMISSION_REQUIRED("Permission required"),
    ACTIVE("Active"),
    ERROR("Error")
}

/**
 * Target applications protected by UNSCROLL.
 */
enum class TargetApp(
    val id: String,
    val displayName: String,
    val packageName: String,
    val defaultDailyLimitMinutes: Int
) {
    INSTAGRAM(
        id = "instagram",
        displayName = "Instagram",
        packageName = "com.instagram.android",
        defaultDailyLimitMinutes = 20
    ),
    YOUTUBE(
        id = "youtube",
        displayName = "YouTube",
        packageName = "com.google.android.youtube",
        defaultDailyLimitMinutes = 30
    ),
    TIKTOK(
        id = "tiktok",
        displayName = "TikTok",
        packageName = "com.zhiliaoapp.musically",
        defaultDailyLimitMinutes = 15
    )
}

/**
 * Individual configuration for a protected application.
 */
data class AppProtectionConfig(
    val app: TargetApp,
    val isProtectionEnabled: Boolean = false, // Starts unconfigured until user explicitly enables
    val blockShortFormOnly: Boolean = true,  // e.g. Reels or Shorts only, allows DMs / long videos
    val dailyLimitMinutes: Int = 20,
    val intentionalPauseSeconds: Int = 10,
    val blockExplorePage: Boolean = true,
    val status: ServiceStatus = ServiceStatus.NOT_CONFIGURED
)

/**
 * Focus Mode session configuration.
 */
data class FocusModeConfig(
    val durationMinutes: Int = 25,
    val isStrictLock: Boolean = false,
    val allowCalls: Boolean = true,
    val ambientSound: Boolean = true
)

/**
 * Focus Mode active session state.
 */
data class ActiveFocusSession(
    val isActive: Boolean = false,
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val startedAtTimestamp: Long = 0L,
    val isPaused: Boolean = false
)

/**
 * Bedtime & Sleep Mode configuration.
 */
data class SleepScheduleConfig(
    val isEnabled: Boolean = false,
    val bedtimeHour: Int = 23,
    val bedtimeMinute: Int = 0,
    val wakeHour: Int = 7,
    val wakeMinute: Int = 0,
    val windDownLeadMinutes: Int = 30,
    val blockAllTargetApps: Boolean = true,
    val enableGrayscaleBedtime: Boolean = true
)

/**
 * Screen time & Interception statistics.
 * Contains only verified measurements. No hardcoded or fabricated numbers.
 */
data class DailyAppUsage(
    val targetApp: TargetApp,
    val usageMinutes: Int,
    val scrollInterceptionsCount: Int,
    val timeSavedMinutes: Int
)

/**
 * Overall summary with transparent data source distinction.
 * Never displays fabricated screen time or reduction percentages when permission is missing.
 */
data class ScreenTimeOverview(
    val totalScreenTimeMinutes: Int = 0,
    val totalInterceptions: Int = 0,
    val totalTimeSavedMinutes: Int = 0,
    val hasUsageData: Boolean = false,
    val isRealUsageStatsAvailable: Boolean = false,
    val dataNotice: String = "Permission required: Grant Android Usage Access in System Settings to view screen time."
)
