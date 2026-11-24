package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AutomationLogDao
import com.example.data.dao.PermittedFileDao
import com.example.data.dao.ScheduledItemDao
import com.example.data.dao.UserPreferenceDao
import com.example.data.entity.AutomationLog
import com.example.data.entity.PermittedFileEntry
import com.example.data.entity.ScheduledItem
import com.example.data.entity.UserPreference

@Database(
    entities = [
        AutomationLog::class,
        ScheduledItem::class,
        UserPreference::class,
        PermittedFileEntry::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun automationLogDao(): AutomationLogDao
    abstract fun scheduledItemDao(): ScheduledItemDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun permittedFileDao(): PermittedFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jarvis_ai_assistant.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
