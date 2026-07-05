/*
 * Schedules and cancels reminder alarms using Android AlarmManager.
 * Connects to: ReminderReceiver.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.breakingthebot.quicknotes.receiver.ReminderReceiver

/**
 * Handles scheduling operations for system notifications.
 */
object ReminderScheduler {
    /**
     * Schedules a local notification at the designated epoch millisecond time.
     *
     * @param context Android context.
     * @param noteId Identifier of the note.
     * @param title Title of the notification.
     * @param body Body content preview.
     * @param triggerTimeMs Target trigger timestamp.
     */
    fun scheduleReminder(
        context: Context,
        noteId: Int,
        title: String,
        body: String,
        triggerTimeMs: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("noteId", noteId)
            putExtra("title", title)
            putExtra("body", body)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        }
    }

    /**
     * Cancels any active scheduled alarm for the designated note.
     *
     * @param context Android context.
     * @param noteId Identifier of the note.
     */
    fun cancelReminder(context: Context, noteId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            noteId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
