package com.wellsync.app.services
//
//import android.app.Service
//import android.content.Intent
//import android.os.Handler
//import android.os.IBinder
//import android.os.Looper
//import android.util.Log
//import com.wellsync.app.utils.NotificationHelper
//
//class HydrationReminderService : Service() {
//
//    private lateinit var notificationHelper: NotificationHelper
//    private val handler = Handler(Looper.getMainLooper())
//    private var reminderRunnable: Runnable? = null
//    private var intervalMinutes = 60
//
//    companion object {
//        const val EXTRA_INTERVAL = "interval_minutes"
//        const val ACTION_START_REMINDERS = "start_reminders"
//        const val ACTION_STOP_REMINDERS = "stop_reminders"
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//        notificationHelper = NotificationHelper(this)
//    }
//
////    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
////        when (intent?.action) {
////            ACTION_START_REMINDERS -> {
////                intervalMinutes = intent.getIntExtra(EXTRA_INTERVAL, 60)
////
////                // Required foreground notification
////                val notification = notificationHelper.buildForegroundNotification(
////                    "Hydration Reminder Active",
////                    "You will receive periodic hydration reminders"
////                )
////                startForeground(1, notification)
////
////
////
////                startReminders()
////            }
////            ACTION_STOP_REMINDERS -> {
////                stopReminders()
////                stopSelf()
////            }
////        }
////        return START_STICKY
////    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Log.d("HydrationService", "Service started with action: ${intent?.action}") // <--- Add this
//
//        when (intent?.action) {
//            ACTION_START_REMINDERS -> {
//                intervalMinutes = intent.getIntExtra(EXTRA_INTERVAL, 60)
//                Log.d("HydrationService", "Interval set to $intervalMinutes seconds") // <--- Add this
//
//                // ✅ Foreground notification so Android won't kill the service
//                val notification = notificationHelper.buildForegroundNotification(
//                    "Hydration Reminder Active",
//                    "You will receive periodic hydration reminders"
//                )
//                startForeground(1, notification)
//
//                startReminders()
//            }
//            ACTION_STOP_REMINDERS -> {
//                stopReminders()
//                stopForeground(true)
//                stopSelf()
//            }
//        }
//        return START_STICKY
//    }
//
//
//    private fun startReminders() {
//        // Stop existing reminders if any
//        stopReminders()
//
//        reminderRunnable = object : Runnable {
//            override fun run() {
//                // ✅ Add this log line for debugging
//                Log.d("HydrationService", "Reminder triggered")
//                notificationHelper.showHydrationReminder()
//
//                handler.postDelayed(this, intervalMinutes  * 1000L)
//            }
//        }
//
//        // Trigger the first reminder immediately
//        handler.post(reminderRunnable!!)
//    }
//
//    private fun stopReminders() {
//        reminderRunnable?.let { handler.removeCallbacks(it) }
//        reminderRunnable = null
//    }
//
//    override fun onBind(intent: Intent?): IBinder? = null
//
//    override fun onDestroy() {
//        super.onDestroy()
//        stopReminders()
//    }
//}





import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import com.wellsync.app.utils.NotificationHelper

class HydrationReminderService : Service() {

    private lateinit var notificationHelper: NotificationHelper
    private val handler = Handler(Looper.getMainLooper())
    private var reminderRunnable: Runnable? = null
    private var intervalSeconds = 60
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val EXTRA_INTERVAL = "interval_seconds"
        const val ACTION_START_REMINDERS = "start_reminders"
        const val ACTION_STOP_REMINDERS = "stop_reminders"
        private const val TAG = "HydrationService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🏗️ Service created")

        notificationHelper = NotificationHelper(this)

        // Acquire wake lock to prevent system from killing service
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "WellSync::HydrationReminder"
        ).apply {
            setReferenceCounted(false)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "🚀 Service started with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START_REMINDERS -> {
                intervalSeconds = intent.getIntExtra(EXTRA_INTERVAL, 60)
                Log.d(TAG, "⏰ Interval set to $intervalSeconds seconds")

                // Start foreground service IMMEDIATELY to prevent killing
                val notification = notificationHelper.buildForegroundNotification(
                    "Hydration Reminder Active",
                    "You will receive reminders every ${intervalSeconds}s"
                )
                startForeground(1, notification)
                Log.d(TAG, "✅ Foreground service started")

                // Acquire wake lock
                wakeLock?.takeIf { !it.isHeld }?.acquire(15*60*1000L) // 15 minutes
                Log.d(TAG, "🔒 Wake lock acquired")

                startReminders()
            }
            ACTION_STOP_REMINDERS -> {
                Log.d(TAG, "🛑 Stopping reminders")
                stopReminders()

                // Release wake lock
                wakeLock?.takeIf { it.isHeld }?.release()
                Log.d(TAG, "🔓 Wake lock released")

                stopForeground(true)
                stopSelf()
            }
        }

        return START_STICKY
    }

    // FIXED: Proper recurring reminder system
    private fun startReminders() {
        stopReminders()

        reminderRunnable = object : Runnable {
            override fun run() {
                Log.d(TAG, "⏰ Reminder triggered at ${System.currentTimeMillis()}")

                try {
                    notificationHelper.showHydrationReminder()
                    Log.d(TAG, "✅ Reminder notification sent successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to send reminder: ${e.message}", e)
                }

                // Schedule next reminder
                handler.postDelayed(this, intervalSeconds * 1000L)
                Log.d(TAG, "⏭️ Next reminder scheduled in ${intervalSeconds}s")
            }
        }

        // Show first reminder immediately
        handler.post(reminderRunnable!!)
        Log.d(TAG, "🎯 First reminder triggered immediately")
    }
    private fun stopReminders() {
        reminderRunnable?.let {
            handler.removeCallbacks(it)
            Log.d(TAG, "🗑️ Removed existing reminder callbacks")
        }
        reminderRunnable = null
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "💀 Service destroyed")

        stopReminders()

        // Release wake lock on destroy
        wakeLock?.takeIf { it.isHeld }?.release()
        Log.d(TAG, "🔓 Wake lock released on destroy")
    }

    // Handle system trying to kill service
    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(TAG, "📱 App removed from recent apps - service continuing")
        super.onTaskRemoved(rootIntent)
        // Service will restart due to START_STICKY
    }
}