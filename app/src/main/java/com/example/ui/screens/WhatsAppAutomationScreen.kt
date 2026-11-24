package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionType
import com.example.model.ParsedAction
import com.example.ui.MainViewModel
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.theme.CyberDarkCard
import com.example.ui.theme.CyberDarkCardElevated
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonViolet
import kotlinx.coroutines.launch

@Composable
fun WhatsAppAutomationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    var recipientPhone by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var contactSearchQuery by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var aiPrompt by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf("Casual") }
    var useBusiness by remember { mutableStateOf(false) }
    var isScheduling by remember { mutableStateOf(false) }
    var scheduleDelayMinutes by remember { mutableStateOf("15") }
    var isGeneratingAi by remember { mutableStateOf(false) }

    var confirmationAction by remember { mutableStateOf<ParsedAction?>(null) }

    val deviceContacts by viewModel.deviceContacts.collectAsState()
    val quickContacts = if (deviceContacts.isNotEmpty()) {
        deviceContacts.map { it.name to it.phoneNumber }
    } else {
        listOf(
            "Rahul" to "+919876543210",
            "Priya" to "+919876543211",
            "Rohit" to "+919876543212",
            "Work Lead" to "+919876543213"
        )
    }

    val tones = listOf("Casual", "Polite", "Urgent", "Professional", "Hinglish")

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("whatsapp_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "WhatsApp",
                    tint = NeonGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WhatsApp Automation",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Contacts Picker
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SELECT CONTACT (संपर्क चुनें)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = contactSearchQuery,
                        onValueChange = {
                            contactSearchQuery = it
                            viewModel.refreshContacts(it)
                        },
                        label = { Text("Search Contacts") },
                        placeholder = { Text("Type name or number...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search contacts", tint = NeonCyan)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("whatsapp_contact_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickContacts) { (name, phone) ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (recipientName == name) NeonGreen.copy(alpha = 0.2f) else CyberDarkCardElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (recipientName == name) NeonGreen else NeonCyan.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .clickable {
                                        recipientName = name
                                        recipientPhone = phone
                                    }
                                    .testTag("contact_$name")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = name,
                                        tint = if (recipientName == name) NeonGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (recipientName == name) NeonGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("Phone Number / Contact") },
                        placeholder = { Text("e.g. +919876543210") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Phone", tint = NeonGreen)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("whatsapp_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Gemini AI Message Composer
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Compose",
                            tint = NeonViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COMPOSE WITH GEMINI AI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonViolet,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        label = { Text("What do you want to say?") },
                        placeholder = { Text("e.g. Tell Rahul I will reach office in 15 mins due to traffic") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("whatsapp_ai_prompt_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Tone:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(tones) { tone ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selectedTone == tone) NeonViolet.copy(alpha = 0.25f) else CyberDarkCardElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedTone == tone) NeonViolet else Color.Transparent
                                ),
                                modifier = Modifier.clickable { selectedTone = tone }
                            ) {
                                Text(
                                    text = tone,
                                    fontSize = 11.sp,
                                    color = if (selectedTone == tone) NeonViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (aiPrompt.isNotBlank()) {
                                isGeneratingAi = true
                                scope.launch {
                                    val generated = viewModel.composeWithAi(aiPrompt, selectedTone)
                                    messageText = generated
                                    isGeneratingAi = false
                                }
                            }
                        },
                        enabled = !isGeneratingAi && aiPrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_message_button")
                    ) {
                        if (isGeneratingAi) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini generating message...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Smart Message", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Message Editor & TTS Read Aloud
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FINAL MESSAGE (संदेश)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen,
                            letterSpacing = 1.sp
                        )

                        // Read Aloud Button
                        OutlinedButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.ttsManager.speak("Aapka message yeh hai: $messageText")
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("read_aloud_button")
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Read aloud", modifier = Modifier.size(16.dp), tint = NeonCyan)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Read Aloud", fontSize = 11.sp, color = NeonCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Message will appear here, or type directly...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("whatsapp_message_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Settings & Options (WhatsApp Business & Scheduling)
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("WhatsApp Business", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Target WhatsApp Business app", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = useBusiness,
                            onCheckedChange = { useBusiness = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen, checkedTrackColor = NeonGreen.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Schedule for Later", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Queue via WorkManager", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isScheduling,
                            onCheckedChange = { isScheduling = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonAmber, checkedTrackColor = NeonAmber.copy(alpha = 0.5f)),
                            modifier = Modifier.testTag("whatsapp_schedule_switch")
                        )
                    }

                    if (isScheduling) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = scheduleDelayMinutes,
                            onValueChange = { scheduleDelayMinutes = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Delay (Minutes)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("schedule_delay_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // Send / Schedule Primary Action Button with Mandatory Confirmation
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        val recipient = recipientName.ifBlank { recipientPhone.ifBlank { "Contact" } }
                        val action = ParsedAction(
                            actionType = ActionType.WHATSAPP_MESSAGE,
                            platform = if (useBusiness) "WhatsApp Business" else "WhatsApp",
                            recipientName = recipient,
                            recipientNumber = recipientPhone,
                            messageContent = messageText,
                            scheduledEpochMillis = if (isScheduling) {
                                val mins = scheduleDelayMinutes.toLongOrNull() ?: 15L
                                System.currentTimeMillis() + (mins * 60_000L)
                            } else null,
                            scheduledTimeString = if (isScheduling) "In $scheduleDelayMinutes minutes" else null,
                            requiresConfirmation = true,
                            spokenConfirmationPrompt = "Kya aap $recipient ko WhatsApp message bhejna chahte hain?"
                        )
                        confirmationAction = action
                        viewModel.ttsManager.speak(action.spokenConfirmationPrompt)
                    }
                },
                enabled = messageText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("send_whatsapp_button")
            ) {
                Icon(
                    imageVector = if (isScheduling) Icons.Default.Schedule else Icons.Default.Send,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isScheduling) "Review & Schedule WhatsApp" else "Review & Send WhatsApp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (confirmationAction != null) {
        ActionConfirmationDialog(
            action = confirmationAction!!,
            onConfirm = {
                val action = confirmationAction!!
                confirmationAction = null
                if (action.scheduledEpochMillis != null) {
                    val delay = ((action.scheduledEpochMillis - System.currentTimeMillis()) / 60_000L).coerceAtLeast(1L)
                    viewModel.scheduleMessageDirect(
                        actionType = "WHATSAPP_MESSAGE",
                        recipient = action.recipientName ?: action.recipientNumber ?: "Target",
                        content = action.messageContent ?: "",
                        delayMinutes = delay
                    )
                    viewModel.ttsManager.speak("Aapka WhatsApp message schedule kar diya gaya hai.")
                } else {
                    viewModel.sendWhatsAppDirect(
                        phone = action.recipientNumber ?: "",
                        message = action.messageContent ?: "",
                        useBusiness = useBusiness
                    )
                }
            },
            onCancel = {
                confirmationAction = null
                viewModel.ttsManager.speak("WhatsApp sending cancel ho gaya.")
            }
        )
    }
}
