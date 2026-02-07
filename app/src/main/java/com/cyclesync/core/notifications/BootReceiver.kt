package com.cyclesync.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-create the notification channel after boot
            val reminderManager = ReminderManager()
            reminderManager.createNotificationChannel(context)
            // Daily log reminder is re-scheduled; cycle-specific reminders
            // will be re-scheduled when the app is next opened and predictions are loaded
        }
    }
}
