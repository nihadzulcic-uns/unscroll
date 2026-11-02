package com.unscroll.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.unscroll.app.MainActivity
import com.unscroll.app.UnscrollApplication

/**
 * Service that displays a full-screen or banner overlay with a calming breathing cycle
 * before the user is sucked into an algorithmic short-form scroll loop.
 */
class FocusOverlayService : Service() {

    companion object {
        const val ACTION_SHOW_INTERCEPT = "action_show_intercept"
        const val ACTION_CLOSE_OVERLAY = "action_close_overlay"
        const val EXTRA_APP_NAME = "extra_app_name"
        private const val NOTIFICATION_ID = 1001
    }

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_SHOW_INTERCEPT) {
            val appName = intent.getStringExtra(EXTRA_APP_NAME) ?: "Distracting App"
            val notification = buildForegroundNotification("Protecting your attention in $appName")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            showCalmOverlay(appName)
        } else if (action == ACTION_CLOSE_OVERLAY) {
            removeOverlay()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun showCalmOverlay(appName: String) {
        if (overlayView != null) return

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutParamsType,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // Programmatic fallback view creation if layout inflation is deferred
        val container = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setBackgroundColor(0xF00B0F0E.toInt()) // Deep charcoal/black with opacity
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Pause. Breathe."
            textSize = 28f
            setTextColor(0xFF34D399.toInt()) // Soft green accent
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }

        val subtitle = TextView(this).apply {
            text = "You opened $appName. Is this an intentional choice, or an automatic reflex?"
            textSize = 16f
            setTextColor(0xFFE2E8F0.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }

        val btnClose = Button(this).apply {
            text = "Put Down Phone"
            setBackgroundColor(0xFF10B981.toInt())
            setTextColor(0xFF064E3B.toInt())
            setOnClickListener {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)
                removeOverlay()
            }
        }

        val btnContinue = Button(this).apply {
            text = "Continue with 10s Intention"
            setBackgroundColor(0x2034D399.toInt())
            setTextColor(0xFF94A3B8.toInt())
            setOnClickListener {
                removeOverlay()
            }
        }

        container.addView(title)
        container.addView(subtitle)
        container.addView(btnClose)
        container.addView(btnContinue)

        overlayView = container
        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay() {
        if (overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    private fun buildForegroundNotification(content: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, UnscrollApplication.FOCUS_CHANNEL_ID)
            .setContentTitle("UNSCROLL Mindful Barrier")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }
}
