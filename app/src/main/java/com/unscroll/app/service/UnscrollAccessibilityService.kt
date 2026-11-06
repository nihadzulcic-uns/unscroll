package com.unscroll.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.unscroll.app.data.model.TargetApp
import com.unscroll.app.data.repository.SettingsRepository

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
        @Volatile
        var isServiceRunning: Boolean = false
            private set
    }

    private var activeTargetPackage: String? = null
    private var isFeedCurrentlyIntercepted = false
    private lateinit var settingsRepository: SettingsRepository
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext)
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
        // The service is filtered to Instagram and YouTube only, so it cannot observe
        // the launcher while the user changes settings. Refresh on each new app window
        // (not every scroll/content event) to pick up settings changed outside the target app.
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            isFeedCurrentlyIntercepted = false
            settingsRepository.refreshProtectionConfigs()
        }
        val targetPackageChanged = packageName != activeTargetPackage
        if (targetPackageChanged) {
            activeTargetPackage = packageName.takeIf {
                it == TargetApp.INSTAGRAM.packageName || it == TargetApp.YOUTUBE.packageName
            }
            isFeedCurrentlyIntercepted = false
        }

        when (packageName) {
            TargetApp.INSTAGRAM.packageName -> {
                if (!settingsRepository.instagramConfig.value.isProtectionEnabled ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))) {
                    isFeedCurrentlyIntercepted = false
                    return
                }

                val rootNode = try {
                    rootInActiveWindow
                } catch (e: Exception) {
                    null
                } ?: return // A temporary missing window root is not proof that the user left Reels.

                try {
                    inspectInstagram(rootNode)
                } finally {
                    rootNode.recycle()
                }
            }

            TargetApp.YOUTUBE.packageName -> {
                if (!settingsRepository.youtubeConfig.value.isProtectionEnabled ||
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))) {
                    isFeedCurrentlyIntercepted = false
                    return
                }

                val rootNode = try {
                    rootInActiveWindow
                } catch (e: Exception) {
                    null
                } ?: return // A temporary missing window root is not proof that the user left Reels.

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
            listOf("clips_viewer", "reel_viewer", "reel_player")
        )
        if (isReelsActive && !isFeedCurrentlyIntercepted) {
            isFeedCurrentlyIntercepted = triggerMindfulInterception("Instagram Reels")
        } else if (!isReelsActive) {
            isFeedCurrentlyIntercepted = false
        }
    }

    private fun inspectYouTube(node: AccessibilityNodeInfo) {
        val isShortsActive = searchNodesForKeywords(
            node,
            listOf("reel_player_page", "shorts_container", "shorts_player")
        )
        if (isShortsActive && !isFeedCurrentlyIntercepted) {
            isFeedCurrentlyIntercepted = triggerMindfulInterception("YouTube Shorts")
        } else if (!isShortsActive) {
            isFeedCurrentlyIntercepted = false
        }
    }

    /**
     * Traverses UI nodes to identify supported feed containers.
     * Recognized password and text-input nodes are skipped.
     */
    private fun searchNodesForKeywords(
        node: AccessibilityNodeInfo?,
        keywords: List<String>,
        depth: Int = 0,
        visitedNodes: IntArray = intArrayOf(0)
    ): Boolean {
        // Guard against unusually large or deeply nested accessibility trees.
        if (node == null || depth > 40 || visitedNodes[0] >= 2_000) return false
        visitedNodes[0]++

        if (node.isPassword) return false
        val className = node.className?.toString() ?: ""
        if (className.contains("EditText", ignoreCase = true) ||
            className.contains("Input", ignoreCase = true)
        ) {
            return false
        }

        // Inspect resource view IDs only; do not read accessibility descriptions.
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        if (keywords.any { viewId.contains(it.lowercase()) }) return true

        val childCount = node.childCount
        for (i in 0 until childCount) {
            val child = try {
                node.getChild(i)
            } catch (e: Exception) {
                null
            }
            if (child != null) {
                val found = try {
                    searchNodesForKeywords(child, keywords, depth + 1, visitedNodes)
                } finally {
                    child.recycle()
                }
                if (found) return true
            }
        }

        return false
    }

    private fun triggerMindfulInterception(appName: String): Boolean {
        Log.i(TAG, "Feed identifier matched for: $appName")
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
            // Android accepting the request does not guarantee that the overlay window was created.
            mainHandler.postDelayed({
                if (FocusOverlayService.overlayState == FocusOverlayService.OVERLAY_STATE_FAILED) {
                    isFeedCurrentlyIntercepted = false
                    Log.w(TAG, "Overlay creation failed; allowing a later accessibility event to retry")
                }
            }, 2_000L)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start overlay service; feed detection may retry", e)
            return false
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
