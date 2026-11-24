package com.example.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "NotificationListener"

data class ReceivedMessageNotification(
    val id: String,
    val packageName: String,
    val sender: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AssistantNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isListenerConnected.value = true
        Log.d(TAG, "Notification Listener connected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val sbnObj = sbn ?: return
        val pkg = sbnObj.packageName ?: return

        // Filter relevant messaging apps: WhatsApp, Telegram, SMS
        if (pkg.contains("whatsapp") || pkg.contains("telegram") || pkg.contains("messaging") || pkg.contains("mms")) {
            val extras = sbnObj.notification.extras
            val title = extras.getString("android.title") ?: extras.getCharSequence("android.title")?.toString() ?: "Unknown"
            val text = extras.getString("android.text") ?: extras.getCharSequence("android.text")?.toString() ?: ""

            if (text.isNotBlank()) {
                val item = ReceivedMessageNotification(
                    id = sbnObj.key,
                    packageName = pkg,
                    sender = title,
                    messageText = text
                )
                val current = _recentMessages.value.toMutableList()
                current.removeAll { it.id == item.id }
                current.add(0, item)
                _recentMessages.value = current.take(15)
                Log.d(TAG, "New message notification captured from $title ($pkg): $text")
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Option to retain or prune
    }

    override fun onDestroy() {
        super.onDestroy()
        _isListenerConnected.value = false
    }

    companion object {
        private val _isListenerConnected = MutableStateFlow(false)
        val isListenerConnected: StateFlow<Boolean> = _isListenerConnected.asStateFlow()

        private val _recentMessages = MutableStateFlow<List<ReceivedMessageNotification>>(emptyList())
        val recentMessages: StateFlow<List<ReceivedMessageNotification>> = _recentMessages.asStateFlow()
    }
}
