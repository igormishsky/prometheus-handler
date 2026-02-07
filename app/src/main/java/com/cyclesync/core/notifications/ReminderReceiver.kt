package com.cyclesync.core.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cyclesync.R
import com.cyclesync.domain.entity.NotificationPrivacy

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra(ReminderManager.EXTRA_REMINDER_TYPE) ?: return
        val privacyName = intent.getStringExtra(ReminderManager.EXTRA_PRIVACY_LEVEL)
            ?: NotificationPrivacy.HIGH.name
        val privacy = NotificationPrivacy.fromNameOrNull(privacyName) ?: NotificationPrivacy.HIGH

        val (title, body) = getNotificationContent(type, privacy)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply { pendingIntent?.let { setContentIntent(it) } }
            .build()

        try {
            NotificationManagerCompat.from(context).notify(type.hashCode(), notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }
    }

    private fun getNotificationContent(type: String, privacy: NotificationPrivacy): Pair<String, String> {
        return when (privacy) {
            NotificationPrivacy.HIGH -> {
                "CycleSync" to "You have an upcoming event. Open app for details."
            }
            NotificationPrivacy.MEDIUM -> {
                when (type) {
                    ReminderManager.TYPE_PERIOD_UPCOMING -> "Upcoming Event" to "A cycle event is coming in a couple of days."
                    ReminderManager.TYPE_PERIOD_TODAY -> "Event Today" to "A cycle event is expected today."
                    ReminderManager.TYPE_PMS_START -> "Phase Change" to "A new phase may be starting."
                    ReminderManager.TYPE_FERTILE_WINDOW -> "Window Open" to "A tracking window has started."
                    ReminderManager.TYPE_LOG_REMINDER -> "Daily Check-in" to "Don't forget to log today."
                    else -> "CycleSync" to "Open app for details."
                }
            }
            NotificationPrivacy.LOW -> {
                when (type) {
                    ReminderManager.TYPE_PERIOD_UPCOMING -> "Period Coming Soon" to "Your period is predicted to start in about 2 days."
                    ReminderManager.TYPE_PERIOD_TODAY -> "Period Predicted Today" to "Your period is expected to start today."
                    ReminderManager.TYPE_PMS_START -> "PMS Phase Starting" to "Your PMS phase may be starting. Take care of yourself."
                    ReminderManager.TYPE_FERTILE_WINDOW -> "Fertile Window Open" to "Your estimated fertile window has started."
                    ReminderManager.TYPE_LOG_REMINDER -> "Log Your Day" to "Take a moment to track how you're feeling today."
                    else -> "CycleSync" to "Open app for details."
                }
            }
        }
    }
}
