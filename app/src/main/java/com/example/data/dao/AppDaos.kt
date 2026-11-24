package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.AutomationLog
import com.example.data.entity.PermittedFileEntry
import com.example.data.entity.ScheduledItem
import com.example.data.entity.UserPreference
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationLogDao {
    @Query("SELECT * FROM automation_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AutomationLog>>

    @Query("SELECT * FROM automation_logs ORDER BY timestamp DESC LIMIT 5")
    fun getRecentLogs(): Flow<List<AutomationLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AutomationLog): Long

    @Query("DELETE FROM automation_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM automation_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: Long)
}

@Dao
interface ScheduledItemDao {
    @Query("SELECT * FROM scheduled_items ORDER BY scheduledTimeMillis ASC")
    fun getAllScheduled(): Flow<List<ScheduledItem>>

    @Query("SELECT * FROM scheduled_items WHERE isExecuted = 0 ORDER BY scheduledTimeMillis ASC")
    fun getPendingScheduled(): Flow<List<ScheduledItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduled(item: ScheduledItem): Long

    @Update
    suspend fun updateScheduled(item: ScheduledItem)

    @Query("UPDATE scheduled_items SET isExecuted = 1 WHERE id = :id")
    suspend fun markExecuted(id: Long)

    @Query("DELETE FROM scheduled_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface UserPreferenceDao {
    @Query("SELECT * FROM user_preferences ORDER BY updatedAt DESC")
    fun getAllPreferences(): Flow<List<UserPreference>>

    @Query("SELECT * FROM user_preferences WHERE isApproved = 1")
    suspend fun getApprovedPreferencesSync(): List<UserPreference>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPreference(pref: UserPreference)

    @Query("DELETE FROM user_preferences WHERE prefKey = :key")
    suspend fun deletePreference(key: String)

    @Query("DELETE FROM user_preferences")
    suspend fun clearPreferences()
}

@Dao
interface PermittedFileDao {
    @Query("SELECT * FROM permitted_files ORDER BY addedAt DESC")
    fun getAllFiles(): Flow<List<PermittedFileEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(entry: PermittedFileEntry): Long

    @Query("UPDATE permitted_files SET folderCategory = :folder WHERE id = :id")
    suspend fun updateFolder(id: Long, folder: String)

    @Query("DELETE FROM permitted_files WHERE id = :id")
    suspend fun deleteFile(id: Long)
}
