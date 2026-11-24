package com.example.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.text.DecimalFormat

private const val TAG = "StorageHelper"

data class StorageStats(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val usedPercent: Float,
    val totalFormatted: String,
    val freeFormatted: String,
    val usedFormatted: String
)

object StorageHelper {

    fun getInternalStorageStats(): StorageStats {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val total = stat.totalBytes
            val free = stat.availableBytes
            val used = (total - free).coerceAtLeast(0L)
            val percent = if (total > 0) (used.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

            StorageStats(
                totalBytes = total,
                freeBytes = free,
                usedBytes = used,
                usedPercent = percent,
                totalFormatted = formatSize(total),
                freeFormatted = formatSize(free),
                usedFormatted = formatSize(used)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error reading StatFs", e)
            StorageStats(0L, 0L, 0L, 0f, "0 GB", "0 GB", "0 GB")
        }
    }

    fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
            .coerceIn(0, units.lastIndex)
        return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun getFileMetadata(context: Context, uri: Uri): Pair<String, Long> {
        var name = "Selected File"
        var size = 0L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex >= 0) size = cursor.getLong(sizeIndex)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to query file metadata", e)
        }
        return name to size
    }

    fun getWorkspaceFolders(context: Context): List<String> {
        val base = File(context.filesDir, "jarvis_workspace")
        if (!base.exists()) base.mkdirs()
        val defaults = listOf("Documents", "Photos", "Voice Notes", "Work")
        defaults.forEach { folderName ->
            val dir = File(base, folderName)
            if (!dir.exists()) dir.mkdirs()
        }
        return base.listFiles()?.filter { it.isDirectory }?.map { it.name }?.sorted() ?: defaults
    }

    fun createWorkspaceFolder(context: Context, folderName: String): Boolean {
        val safeName = folderName.trim().replace(Regex("[^a-zA-Z0-9_ -]"), "")
        if (safeName.isBlank()) return false
        val dir = File(File(context.filesDir, "jarvis_workspace"), safeName)
        return dir.exists() || dir.mkdirs()
    }
}
