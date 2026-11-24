package com.example.model

enum class ActionType(val displayName: String, val iconName: String) {
    WHATSAPP_MESSAGE("WhatsApp Message", "whatsapp"),
    WHATSAPP_OPEN("Open WhatsApp", "whatsapp"),
    SMS_SEND("Send SMS", "sms"),
    TELEGRAM_MESSAGE("Telegram Message", "telegram"),
    INSTAGRAM_POST("Instagram Post", "instagram"),
    FACEBOOK_POST("Facebook Post", "facebook"),
    X_POST("X (Twitter) Post", "twitter"),
    YOUTUBE_OPEN("Open YouTube", "youtube"),
    SETTINGS_OPEN("Open Settings", "settings"),
    CALL_PHONE("Phone Call", "call"),
    CHECK_MESSAGES("Read Notifications & Reply", "reply"),
    CAMERA_OPEN("Open Camera", "camera"),
    GALLERY_OPEN("Open Photo Picker / Gallery", "photo"),
    FILE_MANAGER_OPEN("Open File & Storage Manager", "folder"),
    STORAGE_INFO("Check Device Storage", "storage"),
    APP_LAUNCH("Open Application", "apps"),
    CLOSE_APP("Close / Minimize App", "home"),
    SCREEN_SCROLL_DOWN("Scroll Screen Down", "scroll"),
    SCREEN_SCROLL_UP("Scroll Screen Up", "scroll"),
    SCREEN_TAP_BUTTON("Tap Screen Button", "touch"),
    SCREEN_TYPE_TEXT("Type Into Focused Field", "keyboard"),
    SCREEN_READ_TEXT("Read Visible Screen Text", "visibility"),
    SCREEN_BACK("Navigate Back", "back"),
    SCREEN_HOME("Go to Home Screen", "home"),
    SAVE_PREFERENCE("Remember User Preference", "memory"),
    MULTI_STEP_PLAN("Multi-Step Automation Plan", "plan"),
    GENERAL_QUERY("JARVIS AI Intelligence", "brain"),
    UNKNOWN("Unknown Command", "help")
}

data class ParsedAction(
    val actionType: ActionType = ActionType.UNKNOWN,
    val platform: String = "JARVIS",
    val recipientName: String? = null,
    val recipientNumber: String? = null,
    val messageContent: String? = null,
    val targetUiLabel: String? = null,
    val targetAppName: String? = null,
    val scheduledTimeString: String? = null,
    val scheduledEpochMillis: Long? = null,
    val requiresConfirmation: Boolean = true,
    val spokenConfirmationPrompt: String = "",
    val spokenSuccessResponse: String = "",
    val isClarificationNeeded: Boolean = false,
    val clarificationQuestion: String? = null,
    val planSteps: List<String> = emptyList(),
    val preferenceKey: String? = null,
    val preferenceValue: String? = null,
    val rawCommand: String = ""
)

data class ChatTurn(
    val id: Long = System.currentTimeMillis(),
    val isUser: Boolean,
    val text: String,
    val actionBadge: String? = null
)

data class TaskExecutionStatus(
    val isRunning: Boolean = false,
    val title: String = "",
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val stepDescription: String = ""
)

sealed interface AssistantVoiceState {
    data object Idle : AssistantVoiceState
    data class WakeWordStandby(val wakeWord: String = "Hey JARVIS") : AssistantVoiceState
    data class Listening(val amplitude: Float = 0f, val partialText: String = "") : AssistantVoiceState
    data class Thinking(val query: String) : AssistantVoiceState
    data class Confirming(val action: ParsedAction) : AssistantVoiceState
    data class Executing(val actionTitle: String) : AssistantVoiceState
    data class Speaking(val spokenText: String, val action: ParsedAction? = null) : AssistantVoiceState
    data class Error(val errorMessage: String) : AssistantVoiceState
}

enum class AssistantLanguage(val code: String, val displayName: String, val localeTag: String) {
    HINGLISH("hi-en", "Hinglish (हिन्दी + English)", "hi-IN"),
    HINDI("hi", "हिन्दी (Hindi)", "hi-IN"),
    ENGLISH("en", "English", "en-US")
}
