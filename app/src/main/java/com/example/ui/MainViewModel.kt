package com.example.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiBrainService
import com.example.ai.SocialCaptionResult
import com.example.automation.AutomationScheduler
import com.example.automation.IntentAutomator
import com.example.data.AppDatabase
import com.example.data.entity.AutomationLog
import com.example.data.entity.PermittedFileEntry
import com.example.data.entity.ScheduledItem
import com.example.data.entity.UserPreference
import com.example.model.ActionType
import com.example.model.AssistantLanguage
import com.example.model.AssistantVoiceState
import com.example.model.ChatTurn
import com.example.model.ParsedAction
import com.example.model.TaskExecutionStatus
import com.example.service.AssistantNotificationListenerService
import com.example.service.HandsFreeAccessibilityService
import com.example.service.JarvisForegroundService
import com.example.service.ReceivedMessageNotification
import com.example.speech.SpeechManager
import com.example.speech.TtsManager
import com.example.util.ContactsHelper
import com.example.util.DeviceContact
import com.example.util.StorageHelper
import com.example.util.StorageStats
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "JarvisViewModel"

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication()
    private val db = AppDatabase.getInstance(context)
    private val geminiService = GeminiBrainService()
    val speechManager = SpeechManager(context)
    val ttsManager = TtsManager(context)
    private val scheduler = AutomationScheduler(context)

    private var activeTaskJob: Job? = null
    private var wakeRearmJob: Job? = null

    private val _voiceState = MutableStateFlow<AssistantVoiceState>(AssistantVoiceState.Idle)
    val voiceState: StateFlow<AssistantVoiceState> = _voiceState.asStateFlow()

    private val _isAutomationEnabled = MutableStateFlow(true)
    val isAutomationEnabled: StateFlow<Boolean> = _isAutomationEnabled.asStateFlow()

    private val _isWakeWordEnabled = MutableStateFlow(false)
    val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AssistantLanguage.HINGLISH)
    val currentLanguage: StateFlow<AssistantLanguage> = _currentLanguage.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _pendingAction = MutableStateFlow<ParsedAction?>(null)
    val pendingAction: StateFlow<ParsedAction?> = _pendingAction.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatTurn>>(
        listOf(
            ChatTurn(
                isUser = false,
                text = "Online and at your service, Sir. Say \"Hey JARVIS\" or tap the arc core to begin."
            )
        )
    )
    val chatHistory: StateFlow<List<ChatTurn>> = _chatHistory.asStateFlow()

    private val _taskStatus = MutableStateFlow(TaskExecutionStatus())
    val taskStatus: StateFlow<TaskExecutionStatus> = _taskStatus.asStateFlow()

    private val _storageStats = MutableStateFlow(StorageHelper.getInternalStorageStats())
    val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

    private val _deviceContacts = MutableStateFlow<List<DeviceContact>>(emptyList())
    val deviceContacts: StateFlow<List<DeviceContact>> = _deviceContacts.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val navigationEvents: SharedFlow<String> = _navigationEvents.asSharedFlow()

    // Room Flows
    val recentLogs: StateFlow<List<AutomationLog>> = db.automationLogDao().getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingScheduled: StateFlow<List<ScheduledItem>> = db.scheduledItemDao().getAllScheduled()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<List<UserPreference>> = db.userPreferenceDao().getAllPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val permittedFiles: StateFlow<List<PermittedFileEntry>> = db.permittedFileDao().getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val capturedMessages: StateFlow<List<ReceivedMessageNotification>> =
        AssistantNotificationListenerService.recentMessages

    val isForegroundServiceRunning: StateFlow<Boolean> = JarvisForegroundService.isServiceRunning

    init {
        speechManager.onResultListener = { transcript ->
            Log.d(TAG, "Recognized speech: $transcript")
            handleRecognizedSpeech(transcript)
        }

        speechManager.onTimeoutOrNoMatchListener = {
            if (_isWakeWordEnabled.value && _isAutomationEnabled.value && _voiceState.value is AssistantVoiceState.WakeWordStandby) {
                scheduleWakeWordRearm(1100L)
            }
        }

        speechManager.onErrorListener = { errorMsg ->
            Log.w(TAG, "Speech error: $errorMsg")
            if (_isWakeWordEnabled.value && isForegroundServiceRunning.value) {
                _voiceState.value = AssistantVoiceState.WakeWordStandby()
            } else {
                _voiceState.value = AssistantVoiceState.Error(errorMsg)
            }
        }

        ttsManager.onSpeechCompleted = {
            val currentState = _voiceState.value
            if (currentState is AssistantVoiceState.Speaking && currentState.action == null) {
                if (_isWakeWordEnabled.value && _isAutomationEnabled.value) {
                    _voiceState.value = AssistantVoiceState.WakeWordStandby()
                    scheduleWakeWordRearm(750L)
                } else {
                    _voiceState.value = AssistantVoiceState.Idle
                }
            } else if (_isWakeWordEnabled.value && _isAutomationEnabled.value && currentState is AssistantVoiceState.Speaking) {
                _voiceState.value = AssistantVoiceState.WakeWordStandby()
                scheduleWakeWordRearm(900L)
            }
        }

        refreshContacts("")
    }

    private fun scheduleWakeWordRearm(delayMs: Long = 800L) {
        wakeRearmJob?.cancel()
        wakeRearmJob = viewModelScope.launch {
            delay(delayMs)
            val hasMic = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (hasMic &&
                _isWakeWordEnabled.value &&
                _isAutomationEnabled.value &&
                _voiceState.value is AssistantVoiceState.WakeWordStandby &&
                _pendingAction.value == null &&
                speechManager.isAvailable() &&
                !speechManager.isListening.value
            ) {
                speechManager.startListening(_currentLanguage.value)
            }
        }
    }

    private fun handleRecognizedSpeech(rawTranscript: String) {
        val lower = rawTranscript.lowercase().trim()
        if (_voiceState.value is AssistantVoiceState.WakeWordStandby || _isWakeWordEnabled.value) {
            val wakeTriggers = listOf("hey jarvis", "jarvis", "हे जार्विस", "जार्विस")
            val matchedTrigger = wakeTriggers.firstOrNull { lower.contains(it) }
            if (_voiceState.value is AssistantVoiceState.WakeWordStandby) {
                if (matchedTrigger != null) {
                    val afterWake = lower.substringAfter(matchedTrigger).trim()
                    if (afterWake.isBlank()) {
                        val reply = "Yes sir, I am listening."
                        _voiceState.value = AssistantVoiceState.Speaking(reply, null)
                        ttsManager.speak(reply, _currentLanguage.value)
                        return
                    } else {
                        processCommand(afterWake)
                        return
                    }
                } else {
                    // Ignore non-wake-word chatter in standby mode and re-arm
                    _voiceState.value = AssistantVoiceState.WakeWordStandby()
                    scheduleWakeWordRearm(900L)
                    return
                }
            }
        }
        processCommand(rawTranscript)
    }

    fun toggleWakeWordService() {
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!_isWakeWordEnabled.value) {
            if (!hasMic) {
                _voiceState.value = AssistantVoiceState.Error("Microphone permission is required for Hey JARVIS wake word.")
                return
            }
            _isWakeWordEnabled.value = true
            JarvisForegroundService.start(context)
            _voiceState.value = AssistantVoiceState.WakeWordStandby()
            ttsManager.speak("Hey JARVIS always-on wake word activated.", _currentLanguage.value)
            scheduleWakeWordRearm(1800L)
            logAction("Wake Word Activated", "WAKE_WORD", "Hey JARVIS standby enabled", "SUCCESS", "Foreground service running")
        } else {
            wakeRearmJob?.cancel()
            _isWakeWordEnabled.value = false
            JarvisForegroundService.stop(context)
            speechManager.stopListening()
            _voiceState.value = AssistantVoiceState.Idle
            ttsManager.speak("Wake word standby paused.", _currentLanguage.value)
        }
    }

    fun startListening() {
        if (!_isAutomationEnabled.value) {
            ttsManager.speak("JARVIS automation is paused. Enable it from the dashboard.", _currentLanguage.value)
            return
        }
        _voiceState.value = AssistantVoiceState.Listening()
        speechManager.startListening(_currentLanguage.value)
    }

    fun stopListening() {
        speechManager.stopListening()
        _voiceState.value = if (_isWakeWordEnabled.value) {
            AssistantVoiceState.WakeWordStandby()
        } else {
            AssistantVoiceState.Idle
        }
    }

    fun toggleListening() {
        if (speechManager.isListening.value) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun processCommand(commandText: String) {
        if (commandText.isBlank()) return

        activeTaskJob?.cancel()
        activeTaskJob = viewModelScope.launch {
            appendChat(isUser = true, text = commandText)
            _voiceState.value = AssistantVoiceState.Thinking(commandText)
            try {
                val approvedPrefs = db.userPreferenceDao().getApprovedPreferencesSync()
                val screenContext = HandsFreeAccessibilityService.readCurrentScreenSummary()

                val action = geminiService.parseVoiceCommand(
                    command = commandText,
                    language = _currentLanguage.value,
                    preferences = approvedPrefs,
                    screenContext = screenContext
                )
                Log.d(TAG, "JARVIS parsed action: $action")

                if (action.isClarificationNeeded && !action.clarificationQuestion.isNullOrBlank()) {
                    appendChat(isUser = false, text = action.clarificationQuestion, badge = "Clarification")
                    _voiceState.value = AssistantVoiceState.Speaking(action.clarificationQuestion, null)
                    ttsManager.speak(action.clarificationQuestion, _currentLanguage.value)
                    return@launch
                }

                if (action.actionType == ActionType.GENERAL_QUERY) {
                    val answer = geminiService.askJarvisConversational(
                        question = commandText,
                        language = _currentLanguage.value,
                        history = _chatHistory.value,
                        preferences = approvedPrefs
                    )
                    appendChat(isUser = false, text = answer, badge = "Gemini Brain")
                    _voiceState.value = AssistantVoiceState.Speaking(answer, null)
                    ttsManager.speak(answer, _currentLanguage.value)
                    logAction(commandText, "AI_QUERY", "JARVIS conversational response", "SUCCESS", answer)
                    return@launch
                }

                // Resolve recipient phone number from real contacts if needed
                val resolvedAction = if (!action.recipientName.isNullOrBlank() && action.recipientNumber.isNullOrBlank()) {
                    val foundNum = ContactsHelper.findPhoneNumberByName(context, action.recipientName)
                    if (foundNum != null) action.copy(recipientNumber = foundNum) else action
                } else action

                if (resolvedAction.requiresConfirmation) {
                    _pendingAction.value = resolvedAction
                    _voiceState.value = AssistantVoiceState.Confirming(resolvedAction)
                    val prompt = resolvedAction.spokenConfirmationPrompt.ifBlank {
                        "Sir, please confirm ${resolvedAction.actionType.displayName}."
                    }
                    appendChat(isUser = false, text = prompt, badge = "Awaiting Confirmation")
                    ttsManager.speak(prompt, _currentLanguage.value)
                } else {
                    executeAction(resolvedAction)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error processing command", e)
                val fallbackAction = geminiService.parseLocally(commandText)
                if (fallbackAction.requiresConfirmation) {
                    _pendingAction.value = fallbackAction
                    _voiceState.value = AssistantVoiceState.Confirming(fallbackAction)
                } else {
                    executeAction(fallbackAction)
                }
            }
        }
    }

    fun confirmPendingAction() {
        val action = _pendingAction.value ?: return
        _pendingAction.value = null
        executeAction(action)
    }

    fun cancelPendingAction() {
        val action = _pendingAction.value
        _pendingAction.value = null
        _voiceState.value = AssistantVoiceState.Idle
        appendChat(isUser = false, text = "Understood, Sir. Action cancelled.", badge = "Cancelled")
        ttsManager.speak("Action cancel kar diya gaya hai.", _currentLanguage.value)
        if (action != null) {
            logAction(action.rawCommand, action.actionType.name, "User cancelled action", "CANCELLED", "")
        }
    }

    fun executeAction(action: ParsedAction) {
        activeTaskJob?.cancel()
        activeTaskJob = viewModelScope.launch {
            _voiceState.value = AssistantVoiceState.Executing(action.actionType.displayName)

            // Multi-step visual task progress
            val totalSteps = if (action.planSteps.isNotEmpty()) action.planSteps.size else 1
            if (action.planSteps.isNotEmpty()) {
                action.planSteps.forEachIndexed { idx, stepDesc ->
                    _taskStatus.value = TaskExecutionStatus(
                        isRunning = true,
                        title = action.actionType.displayName,
                        currentStep = idx + 1,
                        totalSteps = totalSteps,
                        stepDescription = stepDesc
                    )
                    delay(450L)
                }
            } else {
                _taskStatus.value = TaskExecutionStatus(
                    isRunning = true,
                    title = action.actionType.displayName,
                    currentStep = 1,
                    totalSteps = 1,
                    stepDescription = "Executing ${action.actionType.displayName}"
                )
            }

            // Scheduled action
            if (action.scheduledEpochMillis != null && action.scheduledEpochMillis > System.currentTimeMillis()) {
                val delayMin = ((action.scheduledEpochMillis - System.currentTimeMillis()) / 60000L).coerceAtLeast(1L)
                scheduler.scheduleMessage(
                    actionType = action.actionType.name,
                    recipient = action.recipientName ?: action.recipientNumber ?: "Target",
                    content = action.messageContent ?: "",
                    delayMinutes = delayMin
                )
                val speech = "Task scheduled for ${action.scheduledTimeString ?: "$delayMin minutes from now"}."
                appendChat(isUser = false, text = speech, badge = "Scheduled")
                _voiceState.value = AssistantVoiceState.Speaking(speech, null)
                ttsManager.speak(speech, _currentLanguage.value)
                logAction(action.rawCommand, action.actionType.name, "Scheduled task", "PENDING", action.messageContent ?: "")
                _taskStatus.value = TaskExecutionStatus()
                return@launch
            }

            var success = false
            var customSpeech: String? = null

            when (action.actionType) {
                ActionType.WHATSAPP_OPEN -> success = IntentAutomator.openWhatsApp(context)
                ActionType.WHATSAPP_MESSAGE -> {
                    success = IntentAutomator.openWhatsApp(
                        context,
                        phoneNumber = action.recipientNumber,
                        message = action.messageContent
                    )
                }
                ActionType.SMS_SEND -> {
                    val phone = action.recipientNumber ?: action.recipientName ?: ""
                    success = IntentAutomator.sendSms(context, phone, action.messageContent ?: "")
                }
                ActionType.TELEGRAM_MESSAGE -> {
                    success = IntentAutomator.openTelegram(context, action.recipientName, action.messageContent)
                }
                ActionType.INSTAGRAM_POST -> {
                    success = IntentAutomator.openInstagramPost(context, action.messageContent ?: "Shared via JARVIS AI")
                }
                ActionType.X_POST -> {
                    success = IntentAutomator.openXPost(context, action.messageContent ?: "")
                }
                ActionType.FACEBOOK_POST -> {
                    success = IntentAutomator.openFacebookPost(context, action.messageContent ?: "")
                }
                ActionType.YOUTUBE_OPEN -> success = IntentAutomator.openYouTube(context)
                ActionType.SETTINGS_OPEN -> success = IntentAutomator.openSettings(context)
                ActionType.CAMERA_OPEN -> success = IntentAutomator.openCamera(context)
                ActionType.GALLERY_OPEN -> {
                    _navigationEvents.tryEmit("media")
                    success = true
                }
                ActionType.FILE_MANAGER_OPEN -> {
                    _navigationEvents.tryEmit("files")
                    success = true
                }
                ActionType.STORAGE_INFO -> {
                    refreshStorageStats()
                    val stats = _storageStats.value
                    customSpeech = "Phone storage status: ${stats.freeFormatted} free out of ${stats.totalFormatted}."
                    _navigationEvents.tryEmit("files")
                    success = true
                }
                ActionType.APP_LAUNCH -> {
                    success = IntentAutomator.launchAppByName(context, action.targetAppName ?: action.platform)
                }
                ActionType.CLOSE_APP, ActionType.SCREEN_HOME -> {
                    success = HandsFreeAccessibilityService.performGoHome()
                    if (!success) {
                        customSpeech = "Please enable JARVIS Accessibility Service in Permissions to automate home/close actions."
                    }
                }
                ActionType.SCREEN_BACK -> {
                    success = HandsFreeAccessibilityService.performGoBack()
                    if (!success) {
                        customSpeech = "Please enable JARVIS Accessibility Service to navigate back hands-free."
                    }
                }
                ActionType.SCREEN_SCROLL_DOWN -> {
                    success = HandsFreeAccessibilityService.performScroll(forward = true)
                    if (!success) {
                        customSpeech = "Accessibility service needed or no scrollable content found."
                    }
                }
                ActionType.SCREEN_SCROLL_UP -> {
                    success = HandsFreeAccessibilityService.performScroll(forward = false)
                    if (!success) {
                        customSpeech = "Accessibility service needed or no scrollable content found."
                    }
                }
                ActionType.SCREEN_TAP_BUTTON -> {
                    val label = action.targetUiLabel ?: ""
                    success = HandsFreeAccessibilityService.findAndClickNodeByText(label)
                    if (!success) {
                        customSpeech = "Could not find a visible button labeled '$label'."
                    }
                }
                ActionType.SCREEN_TYPE_TEXT -> {
                    success = HandsFreeAccessibilityService.typeTextIntoFocusedField(action.messageContent ?: "")
                }
                ActionType.SCREEN_READ_TEXT -> {
                    val summary = HandsFreeAccessibilityService.readCurrentScreenSummary()
                    customSpeech = summary
                    success = true
                }
                ActionType.SAVE_PREFERENCE -> {
                    val key = action.preferenceKey ?: "note"
                    val value = action.preferenceValue ?: action.messageContent ?: ""
                    if (value.isNotBlank()) {
                        saveUserPreference(key, value)
                        customSpeech = "Preference saved in memory: $key is $value."
                        success = true
                    }
                }
                ActionType.CALL_PHONE -> {
                    val phone = action.recipientNumber ?: action.recipientName ?: ""
                    success = IntentAutomator.makePhoneCall(context, phone, directCall = false)
                }
                ActionType.CHECK_MESSAGES -> {
                    val msgs = capturedMessages.value
                    customSpeech = if (msgs.isNotEmpty()) {
                        val latest = msgs.first()
                        "Latest message from ${latest.sender}: ${latest.messageText}."
                    } else {
                        "No unread message notifications right now, Sir."
                    }
                    success = true
                }
                else -> success = true
            }

            val spokenResponse = customSpeech ?: if (success) {
                action.spokenSuccessResponse.ifBlank { "Action completed, Sir." }
            } else {
                "Unable to complete action automatically. Check permissions."
            }

            _taskStatus.value = TaskExecutionStatus()
            appendChat(isUser = false, text = spokenResponse, badge = action.actionType.displayName)
            _voiceState.value = AssistantVoiceState.Speaking(spokenResponse, action)
            ttsManager.speak(spokenResponse, _currentLanguage.value)

            logAction(
                commandText = action.rawCommand.ifBlank { action.actionType.displayName },
                actionType = action.actionType.name,
                summary = "${action.actionType.displayName} ${if (action.recipientName != null) "for ${action.recipientName}" else ""}",
                status = if (success) "SUCCESS" else "FAILED",
                details = action.messageContent ?: spokenResponse
            )
        }
    }

    // Screen Automation direct triggers
    fun triggerScreenScroll(forward: Boolean) {
        val ok = HandsFreeAccessibilityService.performScroll(forward)
        val msg = if (ok) "Scrolled ${if (forward) "down" else "up"}" else "Enable Accessibility Service first"
        logAction("Screen Scroll", if (forward) "SCREEN_SCROLL_DOWN" else "SCREEN_SCROLL_UP", msg, if (ok) "SUCCESS" else "FAILED", "")
    }

    fun triggerScreenTap(label: String) {
        val ok = HandsFreeAccessibilityService.findAndClickNodeByText(label)
        val msg = if (ok) "Tapped '$label'" else "Button '$label' not found or Accessibility disabled"
        logAction("Screen Tap: $label", "SCREEN_TAP_BUTTON", msg, if (ok) "SUCCESS" else "FAILED", "")
    }

    fun triggerScreenType(text: String) {
        val ok = HandsFreeAccessibilityService.typeTextIntoFocusedField(text)
        val msg = if (ok) "Typed into active field" else "No active editable field found"
        logAction("Screen Type", "SCREEN_TYPE_TEXT", msg, if (ok) "SUCCESS" else "FAILED", text)
    }

    fun triggerScreenBack() {
        HandsFreeAccessibilityService.performGoBack()
    }

    fun triggerScreenHome() {
        HandsFreeAccessibilityService.performGoHome()
    }

    // Media & WhatsApp sharing
    fun shareMediaToWhatsApp(uri: Uri, caption: String, mimeType: String, useBusiness: Boolean) {
        val ok = IntentAutomator.shareMediaToWhatsApp(context, uri, caption, mimeType, useBusiness)
        logAction("Media Share WhatsApp", "MEDIA_SHARE", "Shared media to WhatsApp", if (ok) "SUCCESS" else "FAILED", caption)
    }

    fun shareMediaGeneric(uri: Uri, mimeType: String, caption: String) {
        val ok = IntentAutomator.shareMediaOrDocument(context, uri, mimeType, caption)
        logAction("Media/File Share", "MEDIA_SHARE", "Shared file/media", if (ok) "SUCCESS" else "FAILED", caption)
    }

    suspend fun analyzeImageWithJarvis(bitmap: Bitmap, prompt: String): String {
        return geminiService.analyzeSelectedImage(bitmap, prompt)
    }

    // Direct Screen Automation Actions
    fun sendWhatsAppDirect(phone: String, message: String, useBusiness: Boolean) {
        val success = IntentAutomator.openWhatsApp(context, phone, message, useBusiness)
        logAction("WhatsApp direct compose", "WHATSAPP_MESSAGE", "WhatsApp to $phone", if (success) "SUCCESS" else "FAILED", message)
    }

    fun sendSmsDirect(phone: String, message: String) {
        val success = IntentAutomator.sendSms(context, phone, message)
        logAction("SMS direct send", "SMS_SEND", "SMS to $phone", if (success) "SUCCESS" else "FAILED", message)
    }

    fun sendTelegramDirect(target: String, message: String) {
        val success = IntentAutomator.openTelegram(context, target, message)
        logAction("Telegram direct", "TELEGRAM_MESSAGE", "Telegram to $target", if (success) "SUCCESS" else "FAILED", message)
    }

    fun openSocialPost(platform: String, content: String) {
        val success = when (platform.lowercase()) {
            "instagram" -> IntentAutomator.openInstagramPost(context, content)
            "x", "twitter" -> IntentAutomator.openXPost(context, content)
            "facebook" -> IntentAutomator.openFacebookPost(context, content)
            else -> false
        }
        logAction("Social Share: $platform", "${platform.uppercase()}_POST", "Post to $platform", if (success) "SUCCESS" else "FAILED", content)
    }

    fun scheduleMessageDirect(actionType: String, recipient: String, content: String, delayMinutes: Long) {
        viewModelScope.launch {
            scheduler.scheduleMessage(actionType, recipient, content, delayMinutes)
            logAction("Manual Schedule", actionType, "Scheduled for $recipient in $delayMinutes min", "PENDING", content)
        }
    }

    fun cancelScheduleItem(id: Long, tag: String) {
        viewModelScope.launch {
            scheduler.cancelScheduled(id, tag)
        }
    }

    // User-Approved Preferences Memory
    fun saveUserPreference(key: String, value: String) {
        viewModelScope.launch {
            db.userPreferenceDao().upsertPreference(UserPreference(prefKey = key.trim(), prefValue = value.trim(), isApproved = true))
        }
    }

    fun deleteUserPreference(key: String) {
        viewModelScope.launch {
            db.userPreferenceDao().deletePreference(key)
        }
    }

    // Permitted Files & Storage Management
    fun refreshStorageStats() {
        _storageStats.value = StorageHelper.getInternalStorageStats()
    }

    fun addPermittedFile(uri: Uri, mimeType: String, folderCategory: String = "Documents") {
        viewModelScope.launch {
            val (name, size) = StorageHelper.getFileMetadata(context, uri)
            db.permittedFileDao().insertFile(
                PermittedFileEntry(
                    displayName = name,
                    uriString = uri.toString(),
                    mimeType = mimeType,
                    folderCategory = folderCategory,
                    sizeBytes = size
                )
            )
            logAction("Add Permitted File", "FILE_MANAGER", "Indexed $name in $folderCategory", "SUCCESS", "")
        }
    }

    fun moveFileToFolder(id: Long, folder: String) {
        viewModelScope.launch {
            db.permittedFileDao().updateFolder(id, folder)
        }
    }

    fun deletePermittedFileEntry(id: Long) {
        viewModelScope.launch {
            db.permittedFileDao().deleteFile(id)
        }
    }

    fun refreshContacts(query: String) {
        viewModelScope.launch {
            _deviceContacts.value = ContactsHelper.searchContacts(context, query)
        }
    }

    suspend fun composeWithAi(prompt: String, tone: String): String {
        return geminiService.composeMessage(prompt, tone, _currentLanguage.value.displayName)
    }

    suspend fun generateSocialPostWithAi(topic: String, platform: String, tone: String): SocialCaptionResult {
        return geminiService.generateSocialPost(topic, platform, tone)
    }

    fun toggleAutomationStatus() {
        val newState = !_isAutomationEnabled.value
        _isAutomationEnabled.value = newState
        val prompt = if (newState) "JARVIS systems online." else "JARVIS automation paused."
        ttsManager.speak(prompt, _currentLanguage.value)
    }

    fun emergencyStopAll() {
        activeTaskJob?.cancel()
        wakeRearmJob?.cancel()
        _taskStatus.value = TaskExecutionStatus()
        _isAutomationEnabled.value = false
        _isWakeWordEnabled.value = false
        JarvisForegroundService.stop(context)
        speechManager.stopListening()
        ttsManager.stop()
        _pendingAction.value = null
        _voiceState.value = AssistantVoiceState.Idle
        appendChat(isUser = false, text = "EMERGENCY STOP EXECUTED. All voice listeners and automations halted.", badge = "KILL SWITCH")
        logAction("EMERGENCY_STOP", "KILL_SWITCH", "Emergency stop triggered", "SUCCESS", "All JARVIS operations halted immediately.")
    }

    fun setLanguage(lang: AssistantLanguage) {
        _currentLanguage.value = lang
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun clearHistory() {
        viewModelScope.launch {
            db.automationLogDao().clearAllLogs()
        }
    }

    private fun appendChat(isUser: Boolean, text: String, badge: String? = null) {
        val current = _chatHistory.value.toMutableList()
        current.add(ChatTurn(isUser = isUser, text = text, actionBadge = badge))
        _chatHistory.value = current.takeLast(40)
    }

    private fun logAction(commandText: String, actionType: String, summary: String, status: String, details: String) {
        viewModelScope.launch {
            try {
                db.automationLogDao().insertLog(
                    AutomationLog(
                        commandText = commandText,
                        actionType = actionType,
                        summary = summary,
                        status = status,
                        details = details
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert log", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
        ttsManager.shutdown()
    }
}
