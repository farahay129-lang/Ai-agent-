package com.example.automation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.entity.AutomationLog

private const val TAG = "ScheduledMessageWorker"
const val CHANNEL_ID = "automation_scheduled_channel"

class ScheduledMessageWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val actionType = inputData.getString("actionType") ?: "SCHEDULED_ACTION"
        val recipient = inputData.getString("recipient") ?: "Unknown"
        val content = inputData.getString("content") ?: ""
        val scheduledId = inputData.getLong("scheduledId", -1L)

        Log.d(TAG, "Executing scheduled automation: $actionType for $recipient")

        createNotificationChannel()

        val notifyIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "scheduler")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduledId.toInt(),
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hands-Free Scheduled Automation")
            .setContentText("$actionType for $recipient: \"$content\"")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Scheduled Action: $actionType\nRecipient: $recipient\nMessage: $content"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)

        // Update database
        try {
            val db = AppDatabase.getInstance(context)
            if (scheduledId > 0) {
                db.scheduledItemDao().markExecuted(scheduledId)
            }
            db.automationLogDao().insertLog(
                AutomationLog(
                    commandText = "Scheduled: $actionType to $recipient",
                    actionType = actionType,
                    summary = "Triggered scheduled automation for $recipient",
                    status = "SUCCESS",
                    details = content
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating database in worker", e)
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
