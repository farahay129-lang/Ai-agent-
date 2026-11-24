package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat

private const val TAG = "ContactsHelper"

data class DeviceContact(
    val name: String,
    val phoneNumber: String
)

object ContactsHelper {

    fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun searchContacts(context: Context, query: String = "", limit: Int = 25): List<DeviceContact> {
        if (!hasPermission(context)) {
            return emptyList()
        }

        val results = mutableListOf<DeviceContact>()
        val seenNumbers = mutableSetOf<String>()

        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            )
            val selection = if (query.isNotBlank()) {
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
            } else null
            val selectionArgs = if (query.isNotBlank()) {
                arrayOf("%$query%", "%$query%")
            } else null

            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext() && results.size < limit) {
                    val name = cursor.getString(nameIdx)?.trim() ?: continue
                    val rawNum = cursor.getString(numIdx)?.trim() ?: continue
                    val normalized = rawNum.replace(" ", "").replace("-", "")
                    if (seenNumbers.add(normalized)) {
                        results.add(DeviceContact(name, rawNum))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying contacts", e)
        }
        return results
    }

    fun findPhoneNumberByName(context: Context, nameQuery: String): String? {
        if (nameQuery.isBlank() || !hasPermission(context)) return null
        return searchContacts(context, nameQuery, limit = 1).firstOrNull()?.phoneNumber
    }
}
