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
 * Native Android Accessibility Service that non-invasively detects algorithmic short-form
 * video containers (Reels, Shorts, TikTok) in real time.
 *
 * PRIVACY GUARANTEE:
 * - NEVER collects passwords, keystrokes, message content, or personal info.
 * - EditText nodes, password fields, and input fields are STRICTLY IGNORED.
 * - Operates 100% on-device with zero network transmissions.
 */
class UnscrollAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "UnscrollService"
        var isServiceRunning: Boolean = false
            private set
    }

    private var lastInterceptTime = 0L
    private val DEBOUNCE_MILLIS = 4000L // Prevent repetitive firing on single scroll
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
        Log.i(TAG, "UNSCROLL Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !isServiceRunning) return

        val packageName = event.packageName?.toString() ?: return
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastInterceptTime < DEBOUNCE_MILLIS) {
            return
        }

        // Check if overlay permission is granted (required to show window overlay)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            return
        }

        val rootNode = try {
            rootInActiveWindow
        } catch (e: Exception) {
            null
        } ?: return

        when (packageName) {
            TargetApp.INSTAGRAM.packageName -> {
                if (settingsRepository.instagramConfig.value.isProtectionEnabled) {
                    inspectInstagram(rootNode, currentTime)
                }
            }
            TargetApp.YOUTUBE.packageName -> {
                if (settingsRepository.youtubeConfig.value.isProtectionEnabled) {
                    inspectYouTube(rootNode, currentTime)
                }
            }
            TargetApp.TIKTOK.packageName, "com.ss.android.ugc.trill" -> {
                if (settingsRepository.tiktokConfig.value.isProtectionEnabled) {
                    inspectTikTok(rootNode, currentTime)
                }
            }
        }
    }

    private fun inspectInstagram(node: AccessibilityNodeInfo, time: Long) {
        val isReelsActive = searchNodesForKeywords(
            node,
            listOf("clips_viewer", "reel_viewer", "reels_tab", "reel_player")
        )
        if (isReelsActive) {
            lastInterceptTime = time
            statsRepository.recordInterception()
            triggerMindfulInterception("Instagram Reels")
        }
    }

    private fun inspectYouTube(node: AccessibilityNodeInfo, time: Long) {
        val isShortsActive = searchNodesForKeywords(
            node,
            listOf("reel_player_page", "shorts_pivot", "shorts_container", "shorts_player")
        )
        if (isShortsActive) {
            lastInterceptTime = time
            statsRepository.recordInterception()
            triggerMindfulInterception("YouTube Shorts")
        }
    }

    private fun inspectTikTok(node: AccessibilityNodeInfo, time: Long) {
        // TikTok is entirely a vertical short-form feed
        lastInterceptTime = time
        statsRepository.recordInterception()
        triggerMindfulInterception("TikTok")
    }

    /**
     * Traverses UI nodes safely.
     * STRICT PRIVACY FILTER: Password fields, EditTexts, and user messages are completely ignored.
     */
    private fun searchNodesForKeywords(node: AccessibilityNodeInfo?, keywords: List<String>): Boolean {
        if (node == null) return false

        // PRIVACY ENFORCEMENT: Never inspect input or password fields
        if (node.isPassword) return false
        val className = node.className?.toString() ?: ""
        if (className.contains("EditText", ignoreCase = true) || className.contains("Input", ignoreCase = true)) {
            return false
        }

        // Only inspect resource view IDs and accessibility descriptions for layout signatures
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val desc = node.contentDescription?.toString()?.lowercase() ?: ""

        for (keyword in keywords) {
            val kw = keyword.lowercase()
            if (viewId.contains(kw) || desc.contains(kw)) {
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
                val found = searchNodesForKeywords(child, keywords)
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
            startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start overlay service", e)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        isServiceRunning = false
        Log.i(TAG, "UNSCROLL Accessibility Service Unbound")
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Log.w(TAG, "UNSCROLL Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        Log.i(TAG, "UNSCROLL Accessibility Service Destroyed")
    }
}
