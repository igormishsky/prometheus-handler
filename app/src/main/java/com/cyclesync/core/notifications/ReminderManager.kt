package com.cyclesync.core.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
        const val EXTRA_REMINDER_TYPE = "reminder_type"
        const val EXTRA_PRIVACY_LEVEL = "privacy_level"

        const val TYPE_PERIOD_UPCOMING = "period_upcoming"
        const val TYPE_PERIOD_TODAY = "period_today"
        const val TYPE_PMS_START = "pms_start"
        const val TYPE_FERTILE_WINDOW = "fertile_window"
        const val TYPE_LOG_REMINDER = "log_reminder"
    }

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders for your cycle events"
        }
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun schedulePeriodReminder(
        context: Context,
        predictedDate: LocalDate,
        daysBeforeNotify: Int = 2,
        privacyLevel: NotificationPrivacy = NotificationPrivacy.HIGH
    ) {
        val notifyDate = predictedDate.minusDays(daysBeforeNotify.toLong())
        if (notifyDate.isBefore(LocalDate.now()) || notifyDate.isEqual(LocalDate.now())) {
            return
        }
        scheduleAlarm(context, notifyDate, TYPE_PERIOD_UPCOMING, 1001, privacyLevel)

        // Also schedule for the day of
        if (predictedDate.isAfter(LocalDate.now())) {
            scheduleAlarm(context, predictedDate, TYPE_PERIOD_TODAY, 1002, privacyLevel)
        }
    }

    fun schedulePmsReminder(
        context: Context,
        pmsDate: LocalDate,
        privacyLevel: NotificationPrivacy = NotificationPrivacy.HIGH
    ) {
        if (pmsDate.isBefore(LocalDate.now()) || pmsDate.isEqual(LocalDate.now())) {
            return
        }
        scheduleAlarm(context, pmsDate, TYPE_PMS_START, 1003, privacyLevel)
    }

    fun scheduleFertileWindowReminder(
        context: Context,
        fertileStartDate: LocalDate,
        privacyLevel: NotificationPrivacy = NotificationPrivacy.HIGH
    ) {
        if (fertileStartDate.isBefore(LocalDate.now()) || fertileStartDate.isEqual(LocalDate.now())) {
            return
        }
        scheduleAlarm(context, fertileStartDate, TYPE_FERTILE_WINDOW, 1004, privacyLevel)
    }

    fun scheduleDailyLogReminder(
        context: Context,
        privacyLevel: NotificationPrivacy = NotificationPrivacy.HIGH
    ) {
        // Schedule for 9 PM daily
        val now = LocalDateTime.now()
        var reminderTime = now.withHour(21).withMinute(0).withSecond(0)
        if (reminderTime.isBefore(now)) {
            reminderTime = reminderTime.plusDays(1)
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_TYPE, TYPE_LOG_REMINDER)
            putExtra(EXTRA_PRIVACY_LEVEL, privacyLevel.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1005,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerMillis = reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancelAllReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        listOf(1001, 1002, 1003, 1004, 1005).forEach { requestCode ->
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
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

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // Schedule for 9 AM on the given date
        val triggerTime = date.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}
