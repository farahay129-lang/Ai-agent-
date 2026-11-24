package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.data.entity.UserPreference
import com.example.model.ActionType
import com.example.model.AssistantLanguage
import com.example.model.ChatTurn
import com.example.model.ParsedAction
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "JarvisBrainService"

data class SocialCaptionResult(
    val caption: String,
    val hashtags: List<String>
)

class GeminiBrainService {

    private val actionIntentSchema = Schema.obj(
        properties = mapOf(
            "actionType" to Schema.string(
                description = "One of: WHATSAPP_MESSAGE, WHATSAPP_OPEN, SMS_SEND, TELEGRAM_MESSAGE, INSTAGRAM_POST, FACEBOOK_POST, X_POST, YOUTUBE_OPEN, SETTINGS_OPEN, CALL_PHONE, CHECK_MESSAGES, CAMERA_OPEN, GALLERY_OPEN, FILE_MANAGER_OPEN, STORAGE_INFO, APP_LAUNCH, CLOSE_APP, SCREEN_SCROLL_DOWN, SCREEN_SCROLL_UP, SCREEN_TAP_BUTTON, SCREEN_TYPE_TEXT, SCREEN_READ_TEXT, SCREEN_BACK, SCREEN_HOME, SAVE_PREFERENCE, MULTI_STEP_PLAN, GENERAL_QUERY, UNKNOWN"
            ),
            "platform" to Schema.string(description = "Target app/module e.g. WhatsApp, SMS, Telegram, Instagram, Facebook, X, YouTube, Settings, Camera, Gallery, Files, ScreenControl, or JARVIS"),
            "recipientName" to Schema.string(description = "Contact person name if applicable", nullable = true),
            "recipientNumber" to Schema.string(description = "Phone number if specified", nullable = true),
            "messageContent" to Schema.string(description = "Message body, typed text, or question", nullable = true),
            "targetUiLabel" to Schema.string(description = "Button or menu text to tap on screen", nullable = true),
            "targetAppName" to Schema.string(description = "Application name to open or close", nullable = true),
            "scheduledTimeString" to Schema.string(description = "Human readable schedule time", nullable = true),
            "scheduledDelayMinutes" to Schema.integer(description = "Delay in minutes from now if scheduled, 0 if immediate", nullable = true),
            "requiresConfirmation" to Schema.boolean(description = "True for sending messages, calls, sharing private media, payments, or deleting data"),
            "spokenConfirmationPrompt" to Schema.string(description = "Clear spoken confirmation prompt in user language"),
            "spokenSuccessResponse" to Schema.string(description = "Natural JARVIS spoken response"),
            "isClarificationNeeded" to Schema.boolean(description = "True if command is ambiguous"),
            "clarificationQuestion" to Schema.string(description = "Clarification question to ask", nullable = true),
            "preferenceKey" to Schema.string(description = "Preference key if user asks JARVIS to remember something", nullable = true),
            "preferenceValue" to Schema.string(description = "Preference value if user asks JARVIS to remember something", nullable = true),
            "planSteps" to Schema.array(
                items = Schema.string(),
                description = "Ordered list of sub-steps if command requires multi-step task planning",
                nullable = true
            )
        ),
        optionalProperties = listOf(
            "recipientName", "recipientNumber", "messageContent",
            "targetUiLabel", "targetAppName", "scheduledTimeString",
            "scheduledDelayMinutes", "clarificationQuestion",
            "preferenceKey", "preferenceValue", "planSteps"
        )
    )

    private val intentModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = actionIntentSchema
            }
        )
    }

    private val generalModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun parseVoiceCommand(
        command: String,
        language: AssistantLanguage,
        preferences: List<UserPreference> = emptyList(),
        screenContext: String = ""
    ): ParsedAction {
        val trimmed = command.trim()
        if (trimmed.isEmpty()) {
            return ParsedAction(
                actionType = ActionType.UNKNOWN,
                spokenSuccessResponse = "Sir, kripya apna command bolein."
            )
        }

        val prefSummary = if (preferences.isNotEmpty()) {
            preferences.joinToString("; ") { "${it.prefKey}=${it.prefValue}" }
        } else "None"

        try {
            val systemPrompt = """
                You are JARVIS, an advanced, polite, intelligent Android AI Personal Assistant.
                The user speaks in Hindi, English, or Hinglish. Current language: ${language.displayName}.
                User-approved preferences memory: [$prefSummary].
                Current visible screen context (non-sensitive): [${screenContext.take(240)}].

                Classify the command into an actionType:
                - App launch: "WhatsApp kholo", "YouTube kholo", "Instagram open karo" -> WHATSAPP_OPEN / YOUTUBE_OPEN / APP_LAUNCH
                - Messaging: "Rahul ko WhatsApp message bhejo..." -> WHATSAPP_MESSAGE (requiresConfirmation: true)
                - Calling: "Priya ko call karo" -> CALL_PHONE (requiresConfirmation: true)
                - Screen control: "Scroll down / neeche scroll karo" -> SCREEN_SCROLL_DOWN; "Scroll up / upar scroll karo" -> SCREEN_SCROLL_UP; "Tap Send / Submit par click karo" -> SCREEN_TAP_BUTTON (targetUiLabel="Send"); "Back jao" -> SCREEN_BACK; "Home screen par jao" -> SCREEN_HOME; "Screen padho" -> SCREEN_READ_TEXT; "Type hello" -> SCREEN_TYPE_TEXT (messageContent="hello")
                - Media & Camera: "Camera kholo" -> CAMERA_OPEN; "Gallery / Photo kholo" -> GALLERY_OPEN
                - Files & Storage: "Storage check karo" -> STORAGE_INFO; "Files kholo" -> FILE_MANAGER_OPEN
                - Remember preference: "Yaad rakho mera naam Aman hai" -> SAVE_PREFERENCE (preferenceKey="user_name", preferenceValue="Aman", requiresConfirmation: true)
                - Notifications: "Mere messages ka reply do" -> CHECK_MESSAGES
                - Multi-step: If user asks to do multiple steps, pick primary action or MULTI_STEP_PLAN and list clear planSteps.
                - General Q&A / Problem Solving: GENERAL_QUERY.

                SECURITY RULES: Never output actions to read passwords/OTPs or bypass Android security. Always set requiresConfirmation=true for sending messages, calls, sharing media, or saving preferences.
                User command: "$trimmed"
            """.trimIndent()

            val response = withTimeout(40_000L) {
                intentModel.generateContent(systemPrompt)
            }

            val jsonStr = response.text
            if (!jsonStr.isNullOrBlank()) {
                val json = JSONObject(jsonStr)
                val typeStr = json.optString("actionType", "UNKNOWN")
                val actionType = try {
                    ActionType.valueOf(typeStr)
                } catch (e: Exception) {
                    ActionType.UNKNOWN
                }

                val delayMin = json.optInt("scheduledDelayMinutes", 0)
                val scheduledEpoch = if (delayMin > 0) {
                    System.currentTimeMillis() + (delayMin.toLong() * 60_000L)
                } else null

                val stepsList = mutableListOf<String>()
                val stepsArr = json.optJSONArray("planSteps")
                if (stepsArr != null) {
                    for (i in 0 until stepsArr.length()) {
                        stepsArr.optString(i)?.takeIf { it.isNotBlank() }?.let { stepsList.add(it) }
                    }
                }

                return ParsedAction(
                    actionType = actionType,
                    platform = json.optString("platform", "JARVIS"),
                    recipientName = json.optString("recipientName").takeIf { it.isNotBlank() },
                    recipientNumber = json.optString("recipientNumber").takeIf { it.isNotBlank() },
                    messageContent = json.optString("messageContent").takeIf { it.isNotBlank() },
                    targetUiLabel = json.optString("targetUiLabel").takeIf { it.isNotBlank() },
                    targetAppName = json.optString("targetAppName").takeIf { it.isNotBlank() },
                    scheduledTimeString = json.optString("scheduledTimeString").takeIf { it.isNotBlank() },
                    scheduledEpochMillis = scheduledEpoch,
                    requiresConfirmation = json.optBoolean("requiresConfirmation", true),
                    spokenConfirmationPrompt = json.optString(
                        "spokenConfirmationPrompt",
                        "Kya aap is action ko confirm karte hain?"
                    ),
                    spokenSuccessResponse = json.optString("spokenSuccessResponse", "Ji sir, command execute ho raha hai."),
                    isClarificationNeeded = json.optBoolean("isClarificationNeeded", false),
                    clarificationQuestion = json.optString("clarificationQuestion").takeIf { it.isNotBlank() },
                    planSteps = stepsList,
                    preferenceKey = json.optString("preferenceKey").takeIf { it.isNotBlank() },
                    preferenceValue = json.optString("preferenceValue").takeIf { it.isNotBlank() },
                    rawCommand = trimmed
                )
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini parsing timed out, falling back to offline parser", e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API error: ${e.message}, falling back to offline parser", e)
        }

        return parseLocally(trimmed)
    }

    /**
     * Offline basic voice command engine: instant zero-latency execution even without internet.
     */
    fun parseLocally(command: String): ParsedAction {
        val lower = command.lowercase()
        return when {
            lower.contains("scroll down") || lower.contains("neeche scroll") -> {
                ParsedAction(ActionType.SCREEN_SCROLL_DOWN, "ScreenControl", requiresConfirmation = false, spokenSuccessResponse = "Neeche scroll kar raha hoon.", rawCommand = command)
            }
            lower.contains("scroll up") || lower.contains("upar scroll") -> {
                ParsedAction(ActionType.SCREEN_SCROLL_UP, "ScreenControl", requiresConfirmation = false, spokenSuccessResponse = "Upar scroll kar raha hoon.", rawCommand = command)
            }
            lower.contains("go back") || lower.contains("back jao") || lower.contains("piche jao") -> {
                ParsedAction(ActionType.SCREEN_BACK, "ScreenControl", requiresConfirmation = false, spokenSuccessResponse = "Back ja raha hoon.", rawCommand = command)
            }
            lower.contains("home screen") || lower.contains("close app") || lower.contains("band karo") -> {
                ParsedAction(ActionType.SCREEN_HOME, "ScreenControl", requiresConfirmation = false, spokenSuccessResponse = "Home screen par ja raha hoon.", rawCommand = command)
            }
            lower.contains("read screen") || lower.contains("screen padho") -> {
                ParsedAction(ActionType.SCREEN_READ_TEXT, "ScreenControl", requiresConfirmation = false, spokenSuccessResponse = "Screen check kar raha hoon.", rawCommand = command)
            }
            lower.startsWith("tap ") || lower.startsWith("click ") -> {
                val target = command.substringAfter(" ").trim()
                ParsedAction(ActionType.SCREEN_TAP_BUTTON, "ScreenControl", targetUiLabel = target, requiresConfirmation = false, spokenSuccessResponse = "$target par tap kar raha hoon.", rawCommand = command)
            }
            lower.contains("camera") -> {
                ParsedAction(ActionType.CAMERA_OPEN, "Camera", requiresConfirmation = false, spokenSuccessResponse = "Camera open kar raha hoon.", rawCommand = command)
            }
            lower.contains("gallery") || lower.contains("photo picker") || lower.contains("photos kholo") -> {
                ParsedAction(ActionType.GALLERY_OPEN, "Photos", requiresConfirmation = false, spokenSuccessResponse = "Photo manager open kar raha hoon.", rawCommand = command)
            }
            lower.contains("storage") -> {
                ParsedAction(ActionType.STORAGE_INFO, "Storage", requiresConfirmation = false, spokenSuccessResponse = "Phone storage check kar raha hoon.", rawCommand = command)
            }
            lower.contains("file") || lower.contains("folder") -> {
                ParsedAction(ActionType.FILE_MANAGER_OPEN, "Files", requiresConfirmation = false, spokenSuccessResponse = "File manager open kar raha hoon.", rawCommand = command)
            }
            lower.contains("whatsapp kholo") || lower.contains("open whatsapp") -> {
                ParsedAction(ActionType.WHATSAPP_OPEN, "WhatsApp", requiresConfirmation = false, spokenSuccessResponse = "WhatsApp khol raha hoon.", rawCommand = command)
            }
            lower.contains("youtube kholo") || lower.contains("open youtube") -> {
                ParsedAction(ActionType.YOUTUBE_OPEN, "YouTube", requiresConfirmation = false, spokenSuccessResponse = "YouTube khol raha hoon.", rawCommand = command)
            }
            lower.contains("setting") -> {
                ParsedAction(ActionType.SETTINGS_OPEN, "Settings", requiresConfirmation = false, spokenSuccessResponse = "Phone ki settings khol raha hoon.", rawCommand = command)
            }
            lower.contains("call ") || lower.contains("phone lagao") -> {
                val recipient = extractRecipient(command)
                ParsedAction(
                    actionType = ActionType.CALL_PHONE,
                    platform = "Phone",
                    recipientName = recipient,
                    requiresConfirmation = true,
                    spokenConfirmationPrompt = "Kya aap ${recipient ?: "is number"} ko call karna chahte hain?",
                    spokenSuccessResponse = "Call dialer open kar raha hoon.",
                    rawCommand = command
                )
            }
            lower.contains("instagram") && (lower.contains("post") || lower.contains("upload")) -> {
                ParsedAction(ActionType.INSTAGRAM_POST, "Instagram", messageContent = command, requiresConfirmation = true, spokenConfirmationPrompt = "Kya aap Instagram post prepare karna chahte hain?", rawCommand = command)
            }
            lower.contains("whatsapp") || lower.contains("message") || lower.contains("bhejo") -> {
                val recipient = extractRecipient(command)
                ParsedAction(
                    actionType = ActionType.WHATSAPP_MESSAGE,
                    platform = "WhatsApp",
                    recipientName = recipient,
                    messageContent = extractContent(command),
                    requiresConfirmation = true,
                    spokenConfirmationPrompt = if (recipient != null) "Kya aap $recipient ko WhatsApp message bhejna chahte hain?" else "Kise message bhejna hai?",
                    spokenSuccessResponse = "WhatsApp message taiyar hai.",
                    rawCommand = command
                )
            }
            lower.contains("sms") -> {
                val recipient = extractRecipient(command)
                ParsedAction(ActionType.SMS_SEND, "SMS", recipientName = recipient, messageContent = extractContent(command), requiresConfirmation = true, spokenConfirmationPrompt = "Kya aap SMS bhejna chahte hain?", rawCommand = command)
            }
            lower.contains("telegram") -> {
                val recipient = extractRecipient(command)
                ParsedAction(ActionType.TELEGRAM_MESSAGE, "Telegram", recipientName = recipient, messageContent = extractContent(command), requiresConfirmation = true, spokenConfirmationPrompt = "Kya aap Telegram message bhejna chahte hain?", rawCommand = command)
            }
            lower.contains("reply") || lower.contains("notification") -> {
                ParsedAction(ActionType.CHECK_MESSAGES, "Notifications", requiresConfirmation = false, spokenSuccessResponse = "Recent notifications check kar raha hoon.", rawCommand = command)
            }
            else -> {
                ParsedAction(ActionType.GENERAL_QUERY, "JARVIS", messageContent = command, requiresConfirmation = false, spokenSuccessResponse = "Command noted: $command", rawCommand = command)
            }
        }
    }

    private fun extractRecipient(text: String): String? {
        val words = text.split(" ")
        val koIndex = words.indexOfFirst { it.equals("ko", ignoreCase = true) }
        if (koIndex > 0) return words[koIndex - 1].replaceFirstChar { it.uppercase() }
        val toIndex = words.indexOfFirst { it.equals("to", ignoreCase = true) }
        if (toIndex >= 0 && toIndex + 1 < words.size) return words[toIndex + 1].replaceFirstChar { it.uppercase() }
        val callIndex = words.indexOfFirst { it.equals("call", ignoreCase = true) }
        if (callIndex >= 0 && callIndex + 1 < words.size) return words[callIndex + 1].replaceFirstChar { it.uppercase() }
        return null
    }

    private fun extractContent(text: String): String {
        val kiIndex = text.indexOf("ki ", ignoreCase = true)
        if (kiIndex >= 0) return text.substring(kiIndex + 3).trim()
        val thatIndex = text.indexOf("that ", ignoreCase = true)
        if (thatIndex >= 0) return text.substring(thatIndex + 5).trim()
        return text
    }

    suspend fun composeMessage(contextPrompt: String, tone: String, language: String): String {
        return try {
            withTimeout(30_000L) {
                val prompt = """
                    You are JARVIS. Write a concise, natural message based on this request:
                    "$contextPrompt"
                    Tone: $tone | Language: $language
                    Output ONLY the final message text without quotes or explanations.
                """.trimIndent()
                generalModel.generateContent(prompt).text?.trim() ?: contextPrompt
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI message composition failed", e)
            contextPrompt
        }
    }

    suspend fun generateSocialPost(topic: String, platform: String, tone: String): SocialCaptionResult {
        return try {
            val schema = Schema.obj(
                properties = mapOf(
                    "caption" to Schema.string(description = "Engaging post caption"),
                    "hashtags" to Schema.array(items = Schema.string(), description = "5 to 8 relevant hashtags")
                )
            )
            val model = Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    responseMimeType = "application/json"
                    responseSchema = schema
                }
            )
            val res = withTimeout(35_000L) {
                model.generateContent("Create an engaging $platform post about '$topic'. Tone: $tone.")
            }
            val json = JSONObject(res.text ?: "{}")
            val caption = json.optString("caption", topic)
            val tagsArray = json.optJSONArray("hashtags")
            val tags = mutableListOf<String>()
            if (tagsArray != null) {
                for (i in 0 until tagsArray.length()) {
                    val tag = tagsArray.getString(i)
                    tags.add(if (tag.startsWith("#")) tag else "#$tag")
                }
            }
            SocialCaptionResult(caption, tags)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate social post", e)
            SocialCaptionResult(topic, listOf("#$platform", "#jarvisai", "#trending"))
        }
    }

    suspend fun analyzeSelectedImage(bitmap: Bitmap, userPrompt: String): String {
        return try {
            withTimeout(45_000L) {
                val res = generalModel.generateContent(
                    content {
                        image(bitmap)
                        text(userPrompt.ifBlank { "Describe this photo concisely and suggest a short WhatsApp/Instagram caption." })
                    }
                )
                res.text?.trim() ?: "Image analyzed."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Vision analysis failed", e)
            "Could not analyze image right now: ${e.localizedMessage ?: "Network/AI error"}"
        }
    }

    suspend fun translate(text: String, toHindi: Boolean): String {
        return try {
            val target = if (toHindi) "Hindi (Devanagari script)" else "fluent English"
            withTimeout(30_000L) {
                generalModel.generateContent("Translate into $target. Output only the translation:\n\"$text\"").text?.trim() ?: text
            }
        } catch (e: Exception) {
            Log.e(TAG, "Translation error", e)
            text
        }
    }

    /**
     * Multi-turn context-aware JARVIS conversation with approved preferences memory.
     */
    suspend fun askJarvisConversational(
        question: String,
        language: AssistantLanguage,
        history: List<ChatTurn>,
        preferences: List<UserPreference>
    ): String {
        return try {
            val prefBlock = if (preferences.isNotEmpty()) {
                "User-approved preferences: " + preferences.joinToString(", ") { "${it.prefKey}: ${it.prefValue}" }
            } else ""

            val chatHistory = history.takeLast(6).map { turn ->
                content(if (turn.isUser) "user" else "model") { text(turn.text) }
            }

            withTimeout(35_000L) {
                val chat = generalModel.startChat(history = chatHistory)
                val prompt = """
                    You are JARVIS, an intelligent, witty, helpful AI Smart Assistant for Android.
                    Respond clearly in 1-3 concise sentences suitable for Text-to-Speech.
                    Preferred language: ${language.displayName}.
                    $prefBlock
                    User says: "$question"
                """.trimIndent()
                chat.sendMessage(prompt).text?.trim() ?: "Samajh gaya sir."
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "JARVIS Q&A failed", e)
            "Kshama karein sir, is waqt online AI se connect nahi ho pa raha."
        }
    }
}
