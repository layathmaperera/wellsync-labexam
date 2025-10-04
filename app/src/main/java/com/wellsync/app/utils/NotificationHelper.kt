package com.wellsync.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.wellsync.app.MainActivity
import java.text.SimpleDateFormat
import java.util.*

class NotificationHelper(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        const val CHANNEL_ID_FOREGROUND = "wellsync_foreground"
        const val CHANNEL_ID_REMINDERS = "wellsync_reminders"
        private const val TAG = "NotificationHelper"
    }

    init {
        Log.d(TAG, "NotificationHelper created")
        createNotificationChannels()
        checkNotificationPermissions()
    }

    private fun createNotificationChannels() {
        Log.d(TAG, "Creating notification channels...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                // Foreground service channel
                val foregroundChannel = NotificationChannel(
                    CHANNEL_ID_FOREGROUND,
                    "Background Service",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Keeps hydration service running"
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                }

                // Enhanced reminder channel for better visibility
                val reminderChannel = NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    "Hydration Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Hydration reminder notifications"
                    setShowBadge(true)
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 300, 300, 300)
                    enableLights(true)
                    lightColor = Color.BLUE
                    setSound(
                        android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,
                        null
                    )
                }

                manager.createNotificationChannel(foregroundChannel)
                manager.createNotificationChannel(reminderChannel)

                Log.d(TAG, "Notification channels created successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error creating channels: ${e.message}", e)
            }
        } else {
            Log.d(TAG, "Android < 8.0, no channels needed")
        }
    }

    private fun checkNotificationPermissions() {
        try {
            val enabled = notificationManager.areNotificationsEnabled()
            Log.d(TAG, "Notifications enabled: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions: ${e.message}", e)
        }
    }

    fun buildForegroundNotification(title: String, content: String): android.app.Notification {
        Log.d(TAG, "Building foreground notification")
        Log.d(TAG, "Title: $title")
        Log.d(TAG, "Content: $content")

        return try {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_FOREGROUND)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setSilent(true)
                .build()

            Log.d(TAG, "Foreground notification built successfully")
            notification

        } catch (e: Exception) {
            Log.e(TAG, "Error building foreground notification: ${e.message}", e)
            throw e
        }
    }

    fun showHydrationReminder() {
        Log.d(TAG, "showHydrationReminder() called")

        if (!notificationManager.areNotificationsEnabled()) {
            Log.w(TAG, "Notifications disabled, cannot show reminder")
            return
        }

        try {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Create unique content for each notification
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val currentTime = timeFormat.format(Date())
            val messages = arrayOf(
                "Time to drink water!",
                "Stay hydrated!",
                "Don't forget your water!",
                "Hydration reminder!",
                "Take a water break!",
                "Keep drinking water!"
            )
            val randomMessage = messages.random()

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
                .setContentTitle("💧 Hydration Alert - $currentTime")
                .setContentText(randomMessage)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                // Force heads-up behavior
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(longArrayOf(0, 300, 300, 300))
                // Make each notification unique
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .build()

            val notificationId = System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notification)

            Log.d(TAG, "Hydration reminder sent with ID: $notificationId at $currentTime")

        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing reminder: ${e.message}", e)
        }
    }

    fun testNotification() {
        Log.d(TAG, "testNotification() called")
        showHydrationReminder()
    }
}