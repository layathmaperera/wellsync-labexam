package com.wellsync.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.wellsync.app.services.HydrationReminderService
import com.wellsync.app.utils.SharedPreferencesHelper

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                restartHydrationReminders(context)
            }
        }
    }

    private fun restartHydrationReminders(context: Context) {
        val prefsHelper = SharedPreferencesHelper(context)

        if (prefsHelper.isHydrationReminderEnabled()) {
            val serviceIntent = Intent(context, HydrationReminderService::class.java).apply {
                action = HydrationReminderService.ACTION_START_REMINDERS
                putExtra(HydrationReminderService.EXTRA_INTERVAL, prefsHelper.getReminderInterval())
            }
            if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }

        }
    }
}
