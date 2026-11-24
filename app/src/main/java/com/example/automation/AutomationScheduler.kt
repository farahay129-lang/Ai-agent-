package com.example.automation

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.AppDatabase
import com.example.data.entity.ScheduledItem
import java.util.concurrent.TimeUnit

private const val TAG = "AutomationScheduler"

class AutomationScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val db = AppDatabase.getInstance(context)

    suspend fun scheduleMessage(
        actionType: String,
        recipient: String,
        content: String,
        delayMinutes: Long
    ): Long {
        val calculatedDelay = delayMinutes.coerceAtLeast(1)
        val scheduledTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(calculatedDelay)
        val tag = "scheduled_${System.currentTimeMillis()}"

        // Save into Room first
        val item = ScheduledItem(
            actionType = actionType,
            recipient = recipient,
            content = content,
            scheduledTimeMillis = scheduledTime,
            isExecuted = false,
            workTag = tag
        )
        val id = db.scheduledItemDao().insertScheduled(item)

        val inputData = Data.Builder()
            .putString("actionType", actionType)
            .putString("recipient", recipient)
            .putString("content", content)
            .putLong("scheduledId", id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ScheduledMessageWorker>()
            .setInitialDelay(calculatedDelay, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag(tag)
            .build()

        workManager.enqueue(workRequest)
        Log.d(TAG, "Enqueued WorkManager task for $actionType with delay $calculatedDelay mins (id: $id)")
        return id
    }

    suspend fun cancelScheduled(id: Long, tag: String) {
        if (tag.isNotBlank()) {
            workManager.cancelAllWorkByTag(tag)
        }
        db.scheduledItemDao().deleteById(id)
    }
}
