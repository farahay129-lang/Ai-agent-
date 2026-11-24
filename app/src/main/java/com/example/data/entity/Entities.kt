package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_logs")
data class AutomationLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val commandText: String,
    val actionType: String,
    val summary: String,
    val status: String, // SUCCESS, PENDING, CANCELLED, FAILED
    val details: String = ""
)

@Entity(tableName = "scheduled_items")
data class ScheduledItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val actionType: String,
    val recipient: String,
    val content: String,
    val scheduledTimeMillis: Long,
    val isExecuted: Boolean = false,
    val workTag: String = ""
)

@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey
    val prefKey: String,
    val prefValue: String,
    val isApproved: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "permitted_files")
data class PermittedFileEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayName: String,
    val uriString: String,
    val mimeType: String,
    val folderCategory: String = "General",
    val sizeBytes: Long = 0L,
    val addedAt: Long = System.currentTimeMillis()
)
