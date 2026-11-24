package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
fun VoiceAssistantScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val voiceState by viewModel.voiceState.collectAsState()
    val isListening by viewModel.speechManager.isListening.collectAsState()
    val amplitude by viewModel.speechManager.rmsAmplitude.collectAsState()
    val partialTranscript by viewModel.speechManager.partialTranscript.collectAsState()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val pendingAction by viewModel.pendingAction.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()

    var textCommand by remember { mutableStateOf("") }
    var showLangMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            listState.animateScrollToItem(chatHistory.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("voice_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "JARVIS Brain & Voice",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CyberDarkCardElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clickable { showLangMenu = true }
                            .testTag("voice_room_lang_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Language, contentDescription = "Language", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentLang.displayName.split(" ")[0],
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NeonCyan
                            )
                        }
                    }

                    DropdownMenu(expanded = showLangMenu, onDismissRequest = { showLangMenu = false }) {
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
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact JARVIS HUD Orb + Live State
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AnimatedAvatarOrb(voiceState = voiceState, sizeDp = 92)
                Column(modifier = Modifier.weight(1f)) {
                    val (stateTitle, stateColor) = when (voiceState) {
                        is AssistantVoiceState.Listening -> "LISTENING..." to NeonCyan
                        is AssistantVoiceState.WakeWordStandby -> "HEY JARVIS STANDBY" to NeonCyan
                        is AssistantVoiceState.Thinking -> "REASONING..." to HoloBlue
                        is AssistantVoiceState.Confirming -> "NEEDS APPROVAL" to NeonAmber
                        is AssistantVoiceState.Executing -> "EXECUTING TASK..." to NeonGreen
                        is AssistantVoiceState.Speaking -> "SPEAKING" to NeonGreen
                        is AssistantVoiceState.Error -> "ALERT" to NeonPink
                        else -> "JARVIS READY" to NeonCyan
                    }
                    Text(stateTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = stateColor, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    AudioWaveformVisualizer(
                        amplitude = amplitude,
                        isSpeaking = voiceState is AssistantVoiceState.Speaking,
                        barCount = 14
                    )
                    if (partialTranscript.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(partialTranscript, fontSize = 12.sp, color = Color.White, maxLines = 1)
                    }
                }
            }

            // Conversational Chat History
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatHistory, key = { it.id }) { turn ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (turn.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (turn.isUser) HoloBlue.copy(alpha = 0.25f) else CyberDarkCard
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .border(
                                    1.dp,
                                    if (turn.isUser) HoloBlue.copy(alpha = 0.45f) else NeonCyan.copy(alpha = 0.25f),
                                    RoundedCornerShape(14.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (!turn.isUser) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("JARVIS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                                        if (!turn.actionBadge.isNullOrBlank()) {
                                            Text(turn.actionBadge, fontSize = 10.sp, color = NeonGreen)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(turn.text, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Bottom Input + Mic Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textCommand,
                    onValueChange = { textCommand = it },
                    placeholder = { Text("Ask JARVIS or give a command...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("jarvis_text_command_input"),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (textCommand.isNotBlank()) {
                                    viewModel.processCommand(textCommand)
                                    textCommand = ""
                                }
                            },
                            modifier = Modifier.testTag("jarvis_send_text_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send command", tint = NeonCyan)
                        }
                    }
                )

                GlowingMicButton(
                    isListening = isListening,
                    onClick = { viewModel.toggleListening() },
                    sizeDp = 56
                )
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
