package com.unscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.unscroll.app.data.model.TargetApp
import com.unscroll.app.data.repository.SettingsRepository
import com.unscroll.app.data.repository.StatsRepository

/**
 * Native Android Accessibility Service that checks selected view IDs for Instagram Reels
 * and YouTube Shorts. TikTok automatic detection is intentionally disabled until reliable
 * feed-specific identifiers can be verified.
 *
 * PRIVACY NOTES:
 * - Feed detection is processed locally on the device.
 * - Recognized password and text-input nodes are skipped.
 * - The service checks selected view IDs only.
 * - It may access parts of the active app's interface to identify feeds.
 */
class UnscrollAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "UnscrollService"
        var isServiceRunning: Boolean = false
            private set
    }

    private var activeTargetPackage: String? = null
    private var isFeedCurrentlyIntercepted = false
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var statsRepository: StatsRepository

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext)
        statsRepository = StatsRepository(applicationContext)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isServiceRunning = true
        activeTargetPackage = null
        isFeedCurrentlyIntercepted = false
        Log.i(TAG, "UNSCROLL Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isServiceRunning) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED
        ) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName != activeTargetPackage) {
            activeTargetPackage = packageName.takeIf {
                it == TargetApp.INSTAGRAM.packageName || it == TargetApp.YOUTUBE.packageName
            }
            isFeedCurrentlyIntercepted = false
        }

        when (packageName) {
            TargetApp.INSTAGRAM.packageName -> {
                // Refresh only for supported target apps, not every system accessibility event.
                settingsRepository.refreshProtectionConfigs()
                if (!settingsRepository.instagramConfig.value.isProtectionEnabled ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))) {
                    isFeedCurrentlyIntercepted = false
                    return
                }

                val rootNode = try {
                    rootInActiveWindow
                } catch (e: Exception) {
                    null
                } ?: return

                try {
                    inspectInstagram(rootNode)
                } finally {
                    rootNode.recycle()
                }
            }

            TargetApp.YOUTUBE.packageName -> {
                settingsRepository.refreshProtectionConfigs()
                if (!settingsRepository.youtubeConfig.value.isProtectionEnabled ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))) {
                    isFeedCurrentlyIntercepted = false
                    return
                }

                val rootNode = try {
                    rootInActiveWindow
                } catch (e: Exception) {
                    null
                } ?: return

                try {
                    inspectYouTube(rootNode)
                } finally {
                    rootNode.recycle()
                }
            }

            TargetApp.TIKTOK.packageName, "com.ss.android.ugc.trill" -> {
                // TikTok feed-specific view identifiers have not been verified.
                // Do not intercept the whole app: that would block chats, profiles and settings too.
                return
            }
        }
    }

    private fun inspectInstagram(node: AccessibilityNodeInfo) {
        val isReelsActive = searchNodesForKeywords(
            node,
            listOf("clips_viewer", "reel_viewer", "reels_tab", "reel_player")
        )
        if (isReelsActive && !isFeedCurrentlyIntercepted) {
            isFeedCurrentlyIntercepted = true
            statsRepository.recordInterception()
            triggerMindfulInterception("Instagram Reels")
        } else if (!isReelsActive) {
            isFeedCurrentlyIntercepted = false
        }
    }

    private fun inspectYouTube(node: AccessibilityNodeInfo) {
        val isShortsActive = searchNodesForKeywords(
            node,
            listOf("reel_player_page", "shorts_pivot", "shorts_container", "shorts_player")
        )
        if (isShortsActive && !isFeedCurrentlyIntercepted) {
            isFeedCurrentlyIntercepted = true
            statsRepository.recordInterception()
            triggerMindfulInterception("YouTube Shorts")
        } else if (!isShortsActive) {
            isFeedCurrentlyIntercepted = false
        }
    }

    /**
     * Traverses UI nodes to identify supported feed containers.
     * Recognized password and text-input nodes are skipped.
     */
    private fun searchNodesForKeywords(node: AccessibilityNodeInfo?, keywords: List<String>): Boolean {
        if (node == null) return false

        // Skip nodes recognized as password or text-input fields
        if (node.isPassword) return false
        val className = node.className?.toString() ?: ""
        if (className.contains("EditText", ignoreCase = true) || className.contains("Input", ignoreCase = true)) {
            return false
        }

        // Inspect resource view IDs only; do not read accessibility descriptions
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        for (keyword in keywords) {
            val kw = keyword.lowercase()
            if (viewId.contains(kw)) {
                return true
            }
        }

        // Recurse children safely
        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = try {
                node.getChild(i)
            } catch (e: Exception) {
                null
            }
            if (child != null) {
                val found = try {
                    searchNodesForKeywords(child, keywords)
                } finally {
                    child.recycle()
                }
                if (found) return true
            }
        }

        return false
    }

    private fun triggerMindfulInterception(appName: String) {
        Log.i(TAG, "Mindful barrier triggered for: $appName")
        val intent = Intent(this, FocusOverlayService::class.java).apply {
            action = FocusOverlayService.ACTION_SHOW_INTERCEPT
            putExtra(FocusOverlayService.EXTRA_APP_NAME, appName)
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start overlay service", e)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceRunning = false
        activeTargetPackage = null
        isFeedCurrentlyIntercepted = false
        Log.i(TAG, "UNSCROLL Accessibility Service Unbound")
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "UNSCROLL Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        activeTargetPackage = null
        isFeedCurrentlyIntercepted = false
        Log.i(TAG, "UNSCROLL Accessibility Service Destroyed")
    }
}
