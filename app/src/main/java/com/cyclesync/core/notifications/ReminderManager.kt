package com.cyclesync.core.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cyclesync.domain.entity.NotificationPrivacy
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor() {

    companion object {
        const val CHANNEL_ID = "cyclesync_reminders"
        const val CHANNEL_NAME = "Cycle Reminders"
        const val CHANNEL_DESCRIPTION = "Reminders for your cycle events"
        const val EXTRA_REMINDER_TYPE = "reminder_type"
        const val EXTRA_PRIVACY_LEVEL = "privacy_level"

        const val TYPE_PERIOD_UPCOMING = "period_upcoming"
        const val TYPE_PERIOD_TODAY = "period_today"
        const val TYPE_PMS_START = "pms_start"
        const val TYPE_FERTILE_WINDOW = "fertile_window"
        const val TYPE_LOG_REMINDER = "log_reminder"

        private const val REQUEST_CODE_PERIOD_UPCOMING = 1001
        private const val REQUEST_CODE_PERIOD_TODAY = 1002
        private const val REQUEST_CODE_PMS = 1003
        private const val REQUEST_CODE_FERTILE = 1004
        private const val REQUEST_CODE_LOG = 1005

        private const val DEFAULT_REMINDER_HOUR = 9
        private const val LOG_REMINDER_HOUR = 21
        private const val DEFAULT_DAYS_BEFORE_PERIOD = 2

        private val ALL_REQUEST_CODES = listOf(
            REQUEST_CODE_PERIOD_UPCOMING,
            REQUEST_CODE_PERIOD_TODAY,
            REQUEST_CODE_PMS,
            REQUEST_CODE_FERTILE,
            REQUEST_CODE_LOG
        )
    }

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(channel)
    }

    fun schedulePeriodReminder(
        context: Context,
        predictedDate: LocalDate,
        daysBeforeNotify: Int = DEFAULT_DAYS_BEFORE_PERIOD,
        privacyLevel: NotificationPrivacy = NotificationPrivacy.HIGH
    ) {
        val today = LocalDate.now()
        val notifyDate = predictedDate.minusDays(daysBeforeNotify.toLong().coerceAtLeast(0))
        if (!notifyDate.isAfter(today)) {
            return
        }
        scheduleAlarm(context, notifyDate, TYPE_PERIOD_UPCOMING, REQUEST_CODE_PERIOD_UPCOMING, privacyLevel)

        if (predictedDate.isAfter(today)) {
            scheduleAlarm(context, predictedDate, TYPE_PERIOD_TODAY, REQUEST_CODE_PERIOD_TODAY, privacyLevel)
        }
    }

    fun schedulePmsReminder(
        context: Context,
        pmsDate: LocalDate,
        privacyLevel: NotificationPrivacy = NotificationPrivacy.HIGH
    ) {
        if (!pmsDate.isAfter(LocalDate.now())) {
            return
        }
        scheduleAlarm(context, pmsDate, TYPE_PMS_START, REQUEST_CODE_PMS, privacyLevel)
    }

    fun scheduleFertileWindowReminder(
        context: Context,
        fertileStartDate: LocalDate,
        privacyLevel: NotificationPrivacy = NotificationPrivacy.HIGH
    ) {
        if (!fertileStartDate.isAfter(LocalDate.now())) {
            return
        }
        scheduleAlarm(context, fertileStartDate, TYPE_FERTILE_WINDOW, REQUEST_CODE_FERTILE, privacyLevel)
    }

    fun scheduleDailyLogReminder(
        context: Context,
        privacyLevel: NotificationPrivacy = NotificationPrivacy.HIGH
    ) {
        val now = LocalDateTime.now()
        var reminderTime = now.withHour(LOG_REMINDER_HOUR).withMinute(0).withSecond(0)
        if (reminderTime.isBefore(now)) {
            reminderTime = reminderTime.plusDays(1)
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, TYPE_LOG_REMINDER)
            putExtra(EXTRA_PRIVACY_LEVEL, privacyLevel.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_LOG,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // SCHEDULE_EXACT_ALARM permission not granted on Android 12+
        }
    }

    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        ALL_REQUEST_CODES.forEach { requestCode ->
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                try {
                    alarmManager.cancel(it)
                } catch (_: Exception) {
                    // Ignore cancellation failures
                }
            }
        }
    }

    private fun scheduleAlarm(
        context: Context,
        date: LocalDate,
        type: String,
        requestCode: Int,
        privacyLevel: NotificationPrivacy
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, type)
            putExtra(EXTRA_PRIVACY_LEVEL, privacyLevel.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTime = date.atTime(DEFAULT_REMINDER_HOUR, 0)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (_: SecurityException) {
            // SCHEDULE_EXACT_ALARM permission not granted on Android 12+
        }
    }
}
