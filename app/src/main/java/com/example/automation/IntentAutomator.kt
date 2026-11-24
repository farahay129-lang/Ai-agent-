package com.example.automation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private const val TAG = "IntentAutomator"

object IntentAutomator {

    fun openWhatsApp(
        context: Context,
        phoneNumber: String? = null,
        message: String? = null,
        useBusiness: Boolean = false
    ): Boolean {
        try {
            val pkg = if (useBusiness) "com.whatsapp.w4b" else "com.whatsapp"
            val encodedMsg = if (!message.isNullOrBlank()) {
                URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
            } else ""

            val cleanPhone = phoneNumber?.filter { it.isDigit() || it == '+' }?.replace("+", "")

            val url = if (!cleanPhone.isNullOrBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMsg"
            } else if (encodedMsg.isNotBlank()) {
                "https://api.whatsapp.com/send?text=$encodedMsg"
            } else {
                null
            }

            if (url != null) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                if (isPackageInstalled(context, pkg)) {
                    context.startActivity(intent)
                    return true
                } else {
                    intent.setPackage(null)
                    context.startActivity(intent)
                    return true
                }
            } else {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch WhatsApp", e)
        }
        return false
    }

    fun shareMediaToWhatsApp(
        context: Context,
        mediaUri: Uri,
        caption: String = "",
        mimeType: String = "image/*",
        useBusiness: Boolean = false
    ): Boolean {
        return try {
            val pkg = if (useBusiness) "com.whatsapp.w4b" else "com.whatsapp"
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, mediaUri)
                if (caption.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, caption)
                }
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (isPackageInstalled(context, pkg)) {
                    setPackage(pkg)
                }
            }
            context.startActivity(Intent.createChooser(intent, "Send via WhatsApp").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share media to WhatsApp", e)
            false
        }
    }

    fun shareMediaOrDocument(
        context: Context,
        uri: Uri,
        mimeType: String = "*/*",
        caption: String = ""
    ): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                if (caption.isNotBlank()) putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Share with JARVIS").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share file/media", e)
            false
        }
    }

    fun openCamera(context: Context): Boolean {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open camera", e)
            false
        }
    }

    fun openGallery(context: Context): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "image/*"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open gallery", e)
            false
        }
    }

    fun sendSms(context: Context, phoneNumber: String, message: String): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission && phoneNumber.isNotBlank()) {
            return try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }

                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                }
                Toast.makeText(context, "SMS sent to $phoneNumber", Toast.LENGTH_SHORT).show()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Direct SMS failed, launching intent fallback", e)
                openSmsApp(context, phoneNumber, message)
            }
        } else {
            return openSmsApp(context, phoneNumber, message)
        }
    }

    private fun openSmsApp(context: Context, phoneNumber: String, message: String): Boolean {
        return try {
            val uri = Uri.parse("smsto:${phoneNumber.ifBlank { "" }}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch SMS intent", e)
            false
        }
    }

    fun openTelegram(context: Context, usernameOrPhone: String? = null, message: String? = null): Boolean {
        return try {
            val encodedMsg = if (!message.isNullOrBlank()) {
                URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
            } else ""

            val target = usernameOrPhone?.trim()?.removePrefix("@")
            val url = if (!target.isNullOrBlank()) {
                "https://t.me/$target"
            } else if (encodedMsg.isNotBlank()) {
                "tg://msg?text=$encodedMsg"
            } else {
                "https://t.me"
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("org.telegram.messenger")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (isPackageInstalled(context, "org.telegram.messenger")) {
                context.startActivity(intent)
                true
            } else {
                intent.setPackage(null)
                context.startActivity(intent)
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Telegram", e)
            false
        }
    }

    fun openInstagramPost(context: Context, caption: String): Boolean {
        return try {
            copyToClipboard(context, "Instagram Caption", caption)
            Toast.makeText(context, "Caption copied! Paste into Instagram.", Toast.LENGTH_LONG).show()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, caption)
                setPackage("com.instagram.android")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (isPackageInstalled(context, "com.instagram.android")) {
                context.startActivity(shareIntent)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Instagram", e)
            false
        }
    }

    fun openXPost(context: Context, text: String): Boolean {
        return try {
            val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8.toString())
            val url = "https://twitter.com/intent/tweet?text=$encoded"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open X", e)
            false
        }
    }

    fun openFacebookPost(context: Context, text: String): Boolean {
        return try {
            copyToClipboard(context, "Facebook Post", text)
            Toast.makeText(context, "Text copied to clipboard! Paste in Facebook.", Toast.LENGTH_SHORT).show()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage("com.facebook.katana")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (isPackageInstalled(context, "com.facebook.katana")) {
                context.startActivity(intent)
            } else {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://m.facebook.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Facebook", e)
            false
        }
    }

    fun openYouTube(context: Context): Boolean {
        return try {
            val pkg = "com.google.android.youtube"
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(web)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open YouTube", e)
            false
        }
    }

    fun launchAppByName(context: Context, appName: String): Boolean {
        val lower = appName.trim().lowercase()
        val knownPkg = when {
            lower.contains("whatsapp") -> "com.whatsapp"
            lower.contains("youtube") -> "com.google.android.youtube"
            lower.contains("instagram") -> "com.instagram.android"
            lower.contains("telegram") -> "org.telegram.messenger"
            lower.contains("facebook") -> "com.facebook.katana"
            lower.contains("twitter") || lower == "x" -> "com.twitter.android"
            lower.contains("camera") -> return openCamera(context)
            lower.contains("gallery") || lower.contains("photos") -> return openGallery(context)
            lower.contains("settings") -> return openSettings(context)
            else -> null
        }

        if (knownPkg != null) {
            val intent = context.packageManager.getLaunchIntentForPackage(knownPkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            }
        }
        return false
    }

    fun openSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Settings", e)
            false
        }
    }

    fun makePhoneCall(context: Context, phoneNumber: String, directCall: Boolean = false): Boolean {
        return try {
            val hasCallPerm = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
            val action = if (directCall && hasCallPerm && phoneNumber.isNotBlank()) {
                Intent.ACTION_CALL
            } else {
                Intent.ACTION_DIAL
            }
            val uri = Uri.parse("tel:${phoneNumber.trim()}")
            val intent = Intent(action, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate call", e)
            false
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}
