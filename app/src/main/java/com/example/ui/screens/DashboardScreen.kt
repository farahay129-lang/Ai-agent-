package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AssistantLanguage
import com.example.model.AssistantVoiceState
import com.example.ui.MainViewModel
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.components.AnimatedAvatarOrb
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.GlowingMicButton
import com.example.ui.theme.CyberDarkCard
import com.example.ui.theme.CyberDarkCardElevated
import com.example.ui.theme.HoloBlue
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToVoice: () -> Unit,
    onNavigateToScreenControl: () -> Unit,
    onNavigateToMedia: () -> Unit,
    onNavigateToFiles: () -> Unit,
    onNavigateToWhatsApp: () -> Unit,
    onNavigateToSms: () -> Unit,
    onNavigateToTelegram: () -> Unit,
    onNavigateToSocial: () -> Unit,
    onNavigateToScheduler: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToPermissions: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val voiceState by viewModel.voiceState.collectAsState()
    val isAutomationOn by viewModel.isAutomationEnabled.collectAsState()
    val isWakeWordOn by viewModel.isWakeWordEnabled.collectAsState()
    val isFgRunning by viewModel.isForegroundServiceRunning.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val pendingAction by viewModel.pendingAction.collectAsState()
    val taskStatus by viewModel.taskStatus.collectAsState()
    val isListening by viewModel.speechManager.isListening.collectAsState()
    val amplitude by viewModel.speechManager.rmsAmplitude.collectAsState()

    var showLangMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(if (isAutomationOn) NeonCyan else NeonPink)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isWakeWordOn && isFgRunning -> "WAKE WORD \"HEY JARVIS\" ACTIVE"
                                isAutomationOn -> "JARVIS SYSTEMS ONLINE"
                                else -> "SYSTEMS STANDBY"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAutomationOn) NeonCyan else NeonPink,
                            letterSpacing = 1.1.sp
                        )
                    }
                    Text(
                        text = "JARVIS AI",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        IconButton(
                            onClick = { showLangMenu = true },
                            modifier = Modifier.testTag("language_selector_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Select language",
                                tint = NeonCyan
                            )
                        }
                        DropdownMenu(
                            expanded = showLangMenu,
                            onDismissRequest = { showLangMenu = false }
                        ) {
                            AssistantLanguage.values().forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.displayName) },
                                    onClick = {
                                        viewModel.setLanguage(lang)
                                        showLangMenu = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = HoloBlue
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Multi-Step Task Status HUD
            item {
                AnimatedVisibility(visible = taskStatus.isRunning) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonCyan, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "EXECUTING: ${taskStatus.title}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                                Text(
                                    text = "Step ${taskStatus.currentStep}/${taskStatus.totalSteps}",
                                    fontSize = 11.sp,
                                    color = NeonGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(taskStatus.stepDescription, fontSize = 13.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = {
                                    if (taskStatus.totalSteps > 0) {
                                        taskStatus.currentStep.toFloat() / taskStatus.totalSteps.toFloat()
                                    } else 1f
                                },
                                modifier = Modifier.fillMaxWidth(),
                                color = NeonCyan
                            )
                        }
                    }
                }
            }

            // Hero Holographic Arc-Reactor AI Orb
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = 1.2.dp,
                            brush = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.65f), HoloBlue.copy(alpha = 0.45f))),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.clickable { onNavigateToVoice() }) {
                            AnimatedAvatarOrb(voiceState = voiceState, sizeDp = 152)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val statusText = when (val state = voiceState) {
                            is AssistantVoiceState.Listening -> "Listening, Sir..."
                            is AssistantVoiceState.WakeWordStandby -> "Standby — Say \"Hey JARVIS\""
                            is AssistantVoiceState.Thinking -> "Gemini Brain processing..."
                            is AssistantVoiceState.Confirming -> "Authorization required for ${state.action.platform}"
                            is AssistantVoiceState.Executing -> "Executing: ${state.actionTitle}"
                            is AssistantVoiceState.Speaking -> state.spokenText.take(58)
                            is AssistantVoiceState.Error -> "Alert: ${state.errorMessage}"
                            else -> if (isAutomationOn) "Tap orb for AI Chat or tap Mic to command" else "Automation paused"
                        }

                        Text(
                            text = statusText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = when (voiceState) {
                                is AssistantVoiceState.Listening -> NeonCyan
                                is AssistantVoiceState.Thinking -> HoloBlue
                                is AssistantVoiceState.Confirming -> NeonAmber
                                is AssistantVoiceState.Speaking -> NeonGreen
                                is AssistantVoiceState.Error -> NeonPink
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AudioWaveformVisualizer(
                            amplitude = amplitude,
                            isSpeaking = voiceState is AssistantVoiceState.Speaking
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        GlowingMicButton(
                            isListening = isListening,
                            onClick = { viewModel.toggleListening() }
                        )
                    }
                }
            }

            // Always-On Wake Word & Engine Toggles
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Hearing, contentDescription = null, tint = if (isWakeWordOn) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Wake Word: \"Hey JARVIS\"", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Foreground Service notification listener", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isWakeWordOn,
                                onCheckedChange = { viewModel.toggleWakeWordService() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan),
                                modifier = Modifier.testTag("wake_word_toggle_switch")
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = if (isAutomationOn) NeonGreen else NeonPink)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("JARVIS Automation Core", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(if (isAutomationOn) "Voice & Screen actions enabled" else "All triggers paused", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isAutomationOn,
                                onCheckedChange = { viewModel.toggleAutomationStatus() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonGreen),
                                modifier = Modifier.testTag("automation_toggle_switch")
                            )
                        }
                    }
                }
            }

            // Voice Commands Quick Strip
            item {
                Column {
                    Text(
                        text = "VOICE COMMANDS (हिंदी / ENGLISH)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val sampleCommands = listOf(
                            "WhatsApp kholo",
                            "Scroll down",
                            "Camera kholo",
                            "Storage check karo",
                            "Rahul ko message bhejo",
                            "Read screen",
                            "Phone ki settings kholo"
                        )
                        items(sampleCommands) { cmd ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = CyberDarkCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .clickable { viewModel.processCommand(cmd) }
                                    .testTag("sample_command_${cmd.replace(" ", "_")}")
                            ) {
                                Text(
                                    text = "\"$cmd\"",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // JARVIS Modules Grid
            item {
                Text(
                    text = "JARVIS CONTROL SYSTEMS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = HoloBlue,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModuleTile(
                            title = "Screen Control",
                            subtitle = "Scroll, tap & read",
                            icon = Icons.Default.TouchApp,
                            tint = NeonCyan,
                            onClick = onNavigateToScreenControl,
                            modifier = Modifier.weight(1f).testTag("module_screen_control")
                        )
                        ModuleTile(
                            title = "Photo & Media",
                            subtitle = "Picker & Vision AI",
                            icon = Icons.Default.PhotoLibrary,
                            tint = HoloBlue,
                            onClick = onNavigateToMedia,
                            modifier = Modifier.weight(1f).testTag("module_media")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModuleTile(
                            title = "Files & Storage",
                            subtitle = "StatFs & Folders",
                            icon = Icons.Default.Folder,
                            tint = NeonAmber,
                            onClick = onNavigateToFiles,
                            modifier = Modifier.weight(1f).testTag("module_files")
                        )
                        ModuleTile(
                            title = "WhatsApp",
                            subtitle = "Contacts & AI send",
                            icon = Icons.Default.Chat,
                            tint = NeonGreen,
                            onClick = onNavigateToWhatsApp,
                            modifier = Modifier.weight(1f).testTag("module_whatsapp")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModuleTile(
                            title = "SMS & Calls",
                            subtitle = "Direct & Dialer",
                            icon = Icons.Default.Send,
                            tint = NeonCyan,
                            onClick = onNavigateToSms,
                            modifier = Modifier.weight(1f).testTag("module_sms")
                        )
                        ModuleTile(
                            title = "Telegram",
                            subtitle = "Instant links",
                            icon = Icons.Default.Send,
                            tint = Color(0xFF26A5E4),
                            onClick = onNavigateToTelegram,
                            modifier = Modifier.weight(1f).testTag("module_telegram")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModuleTile(
                            title = "Social Suite",
                            subtitle = "Insta, FB, X",
                            icon = Icons.Default.Campaign,
                            tint = NeonPink,
                            onClick = onNavigateToSocial,
                            modifier = Modifier.weight(1f).testTag("module_social")
                        )
                        ModuleTile(
                            title = "Scheduler",
                            subtitle = "WorkManager Queue",
                            icon = Icons.Default.Schedule,
                            tint = NeonAmber,
                            onClick = onNavigateToScheduler,
                            modifier = Modifier.weight(1f).testTag("module_scheduler")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ModuleTile(
                            title = "Permissions",
                            subtitle = "Security & Access",
                            icon = Icons.Default.Security,
                            tint = NeonGreen,
                            onClick = onNavigateToPermissions,
                            modifier = Modifier.weight(1f).testTag("module_permissions")
                        )
                        ModuleTile(
                            title = "JARVIS Brain",
                            subtitle = "Chat & Memory",
                            icon = Icons.Default.Hearing,
                            tint = HoloBlue,
                            onClick = onNavigateToVoice,
                            modifier = Modifier.weight(1f).testTag("module_voice_chat")
                        )
                    }
                }
            }

            // Recent Activity Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT SYSTEM LOGS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    TextButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("view_all_history_button")
                    ) {
                        Text("View All", fontSize = 12.sp, color = NeonCyan)
                    }
                }

                if (recentLogs.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Systems initialized. Speak a command or tap any module.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentLogs.take(4).forEach { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (log.status) {
                                                        "SUCCESS" -> NeonGreen
                                                        "PENDING" -> NeonAmber
                                                        "CANCELLED" -> Color.Gray
                                                        else -> NeonPink
                                                    }
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = log.summary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "\"${log.commandText}\"",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Text(
                                        text = log.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (log.status) {
                                            "SUCCESS" -> NeonGreen
                                            "PENDING" -> NeonAmber
                                            "CANCELLED" -> Color.Gray
                                            else -> NeonPink
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Emergency Kill Switch
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.emergencyStopAll() }
                        .testTag("emergency_stop_button"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Emergency Stop", tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EMERGENCY STOP: HALT ALL JARVIS TASKS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFCA5A5),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (pendingAction != null) {
        ActionConfirmationDialog(
            action = pendingAction!!,
            onConfirm = { viewModel.confirmPendingAction() },
            onCancel = { viewModel.cancelPendingAction() }
        )
    }
}

@Composable
fun ModuleTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
