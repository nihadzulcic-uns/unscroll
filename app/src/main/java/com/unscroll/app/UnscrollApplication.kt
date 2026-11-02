package com.unscroll.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class UnscrollApplication : Application() {

    companion object {
        const val FOCUS_CHANNEL_ID = "unscroll_focus_channel"
        const val INTERCEPT_CHANNEL_ID = "unscroll_interceptions"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val focusChannel = NotificationChannel(
                FOCUS_CHANNEL_ID,
                "Focus Mode Sessions",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live status and remaining time for active Focus Mode sessions"
                setShowBadge(false)
            }

            val interceptChannel = NotificationChannel(
                INTERCEPT_CHANNEL_ID,
                "Interceptions & Mindful Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when infinite feeds were paused or time limits were reached"
            }

            notificationManager.createNotificationChannel(focusChannel)
            notificationManager.createNotificationChannel(interceptChannel)
        }
    }
}
