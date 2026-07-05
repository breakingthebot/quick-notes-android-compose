/*
 * Handles reminder alarms and triggers system local notifications.
 * Connects to: Android AlarmManager and MainActivity.
 * Created: 2026-07-05
 */
package com.breakingthebot.quicknotes.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.breakingthebot.quicknotes.MainActivity

/**
 * Triggered by the AlarmManager when a note reminder is due.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getIntExtra("noteId", 0)
        val title = intent.getStringExtra("title") ?: "Note Reminder"
        val body = intent.getStringExtra("body") ?: "Time to check your note."

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "note_reminders_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Note Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggers local notifications for note reminders."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(noteId, notification)
    }
}
