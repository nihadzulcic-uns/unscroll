package com.unscroll.app.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Process
import com.unscroll.app.data.model.DailyAppUsage
import com.unscroll.app.data.model.ScreenTimeOverview
import com.unscroll.app.data.model.TargetApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

/**
 * Repository responsible for querying real on-device screen time metrics via Android UsageStatsManager.
 * Zero fabricated or hardcoded statistics. If permission is not granted, displays clear empty state.
 * Interception counts are stored separately for each local calendar day.
 */
class StatsRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "unscroll_stats_preferences",
        Context.MODE_PRIVATE
    )

    private var activeInterceptionKey = interceptionKeyForToday()
    private val _interceptionCounter = MutableStateFlow(prefs.getInt(activeInterceptionKey, 0))
    val interceptionCounter: StateFlow<Int> = _interceptionCounter.asStateFlow()

    private fun refreshInterceptionCounterForToday() {
        val todayKey = interceptionKeyForToday()
        if (todayKey != activeInterceptionKey) {
            activeInterceptionKey = todayKey
            _interceptionCounter.value = prefs.getInt(todayKey, 0)
        }
    }

    fun recordInterception() {
        refreshInterceptionCounterForToday()
        val next = _interceptionCounter.value + 1
        prefs.edit().putInt(activeInterceptionKey, next).apply()
        _interceptionCounter.value = next
    }

    /**
     * Checks if the user has granted android.permission.PACKAGE_USAGE_STATS in System Settings.
     */
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Retrieves actual screen time only if permission is granted.
     * Never returns fake or fabricated numbers.
     */
    fun getDailyOverview(): ScreenTimeOverview {
        refreshInterceptionCounterForToday()
        val hasPermission = hasUsageStatsPermission()

        if (!hasPermission) {
            // Honest empty state when permission is missing
            return ScreenTimeOverview(
                totalScreenTimeMinutes = 0,
                totalInterceptions = _interceptionCounter.value,
                totalTimeSavedMinutes = 0,
                hasUsageData = false,
                isRealUsageStatsAvailable = false,
                dataNotice = "Permission required: Grant Android Usage Access in System Settings to view screen time."
            )
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return ScreenTimeOverview(
                totalScreenTimeMinutes = 0,
                totalInterceptions = _interceptionCounter.value,
                totalTimeSavedMinutes = 0,
                hasUsageData = false,
                isRealUsageStatsAvailable = false,
                dataNotice = "UsageStatsManager service unavailable on this device."
            )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val stats: List<UsageStats>? = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        )

        if (stats.isNullOrEmpty()) {
            return ScreenTimeOverview(
                totalScreenTimeMinutes = 0,
                totalInterceptions = _interceptionCounter.value,
                totalTimeSavedMinutes = 0,
                hasUsageData = true,
                isRealUsageStatsAvailable = true,
                dataNotice = "No social media usage detected yet today."
            )
        }

        // Android may return more than one UsageStats row for the same package.
        // Sum milliseconds first and convert to minutes once; truncating each row
        // separately loses minutes and can make totals appear inconsistent.
        val targetPackages = setOf(
            TargetApp.INSTAGRAM.packageName,
            TargetApp.YOUTUBE.packageName,
            TargetApp.TIKTOK.packageName
        )
        val totalTargetMinutes = usageMinutesForPackages(stats, targetPackages)

        return ScreenTimeOverview(
            totalScreenTimeMinutes = totalTargetMinutes,
            totalInterceptions = _interceptionCounter.value,
            totalTimeSavedMinutes = 0,
            hasUsageData = true,
            isRealUsageStatsAvailable = true,
            dataNotice = "Live data synced from Android UsageStatsManager."
        )
    }

    /**
     * Retrieves actual breakdown by app.
     * Returns empty list if permission is missing, or actual queried usage.
     */
    fun getAppBreakdown(): List<DailyAppUsage> {
        refreshInterceptionCounterForToday()
        val hasPermission = hasUsageStatsPermission()
        if (!hasPermission) {
            return emptyList()
        }

        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyList()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val now = System.currentTimeMillis()

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startOfDay,
            now
        ) ?: emptyList()

        val igMinutes = usageMinutesForPackages(stats, setOf(TargetApp.INSTAGRAM.packageName))
        val ytMinutes = usageMinutesForPackages(stats, setOf(TargetApp.YOUTUBE.packageName))
        val ttMinutes = usageMinutesForPackages(stats, setOf(TargetApp.TIKTOK.packageName))

        // Do not display apps with zero measured usage. This prevents the
        // breakdown from looking like it is randomly selecting apps when Android
        // has no foreground time recorded for them today.
        return listOf(
            DailyAppUsage(TargetApp.INSTAGRAM, igMinutes, 0, 0),
            DailyAppUsage(TargetApp.YOUTUBE, ytMinutes, 0, 0),
            DailyAppUsage(TargetApp.TIKTOK, ttMinutes, 0, 0)
        )
            .filter { it.usageMinutes > 0 }
            .sortedByDescending { it.usageMinutes }
    }

    /**
     * Sums all matching UsageStats rows before converting to whole minutes.
     * Keeping the calculation in milliseconds avoids per-row truncation.
     */
    private fun usageMinutesForPackages(
        stats: List<UsageStats>,
        packageNames: Set<String>
    ): Int {
        val totalForegroundMillis = stats.asSequence()
            .filter { it.packageName in packageNames }
            .sumOf { it.totalTimeInForeground.coerceAtLeast(0L) }
        return (totalForegroundMillis / 60_000L).toInt()
    }

    private fun interceptionKeyForToday(): String {
        val today = Calendar.getInstance()
        val dateKey = String.format(
            java.util.Locale.US,
            "%04d-%03d",
            today.get(Calendar.YEAR),
            today.get(Calendar.DAY_OF_YEAR)
        )
        return "key_real_interceptions_$dateKey"
    }
}
