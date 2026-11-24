package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
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
fun SmsAutomationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    var recipientPhone by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var aiPrompt by remember { mutableStateOf("") }
    var isScheduling by remember { mutableStateOf(false) }
    var scheduleDelayMinutes by remember { mutableStateOf("10") }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var confirmationAction by remember { mutableStateOf<ParsedAction?>(null) }

    val deviceContacts by viewModel.deviceContacts.collectAsState()
    val quickContacts = if (deviceContacts.isNotEmpty()) {
        deviceContacts.map { it.name to it.phoneNumber }
    } else {
        listOf(
            "Rahul" to "+919876543210",
            "Priya" to "+919876543211",
            "Home" to "+919876543299",
            "Doctor" to "+919876543288"
        )
    }

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
                    modifier = Modifier.testTag("sms_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "SMS",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SMS Automation",
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
            // Contacts Picker
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "RECIPIENT (प्राप्तकर्ता)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(quickContacts) { (name, phone) ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (recipientName == name) NeonCyan.copy(alpha = 0.2f) else CyberDarkCardElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (recipientName == name) NeonCyan else Color.Transparent
                                ),
                                modifier = Modifier
                                    .clickable {
                                        recipientName = name
                                        recipientPhone = phone
                                    }
                                    .testTag("sms_contact_$name")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = name, tint = NeonCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = recipientPhone,
                        onValueChange = { recipientPhone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("e.g. +919876543210") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sms_phone_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // AI Composer
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COMPOSE SMS WITH AI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonViolet, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        label = { Text("Describe SMS") },
                        placeholder = { Text("e.g. Remind Priya about our 4 PM meeting") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sms_ai_prompt_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (aiPrompt.isNotBlank()) {
                                isGeneratingAi = true
                                scope.launch {
                                    messageText = viewModel.composeWithAi(aiPrompt, "Concise SMS")
                                    isGeneratingAi = false
                                }
                            }
                        },
                        enabled = !isGeneratingAi && aiPrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sms_generate_button")
                    ) {
                        if (isGeneratingAi) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Writing SMS...")
                        } else {
                            Text("Draft SMS with Gemini", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Message Editor & TTS
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
                        Text("SMS TEXT (संदेश)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen, letterSpacing = 1.sp)
                        OutlinedButton(
                            onClick = {
                                if (messageText.isNotBlank()) viewModel.ttsManager.speak("SMS content: $messageText")
                            },
                            shape = RoundedCornerShape(20.dp)
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
                        placeholder = { Text("SMS content...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("sms_message_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Scheduling
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
                            Text("Schedule SMS", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Dispatch automatically at a set time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isScheduling,
                            onCheckedChange = { isScheduling = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonAmber, checkedTrackColor = NeonAmber.copy(alpha = 0.5f))
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
                                .testTag("sms_schedule_delay_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // Send & Call Buttons
            Button(
                onClick = {
                    val phone = recipientPhone.ifBlank { recipientName }
                    if (phone.isNotBlank() && messageText.isNotBlank()) {
                        val action = ParsedAction(
                            actionType = ActionType.SMS_SEND,
                            platform = "SMS",
                            recipientName = recipientName.ifBlank { phone },
                            recipientNumber = recipientPhone,
                            messageContent = messageText,
                            scheduledEpochMillis = if (isScheduling) {
                                val mins = scheduleDelayMinutes.toLongOrNull() ?: 10L
                                System.currentTimeMillis() + (mins * 60_000L)
                            } else null,
                            scheduledTimeString = if (isScheduling) "In $scheduleDelayMinutes minutes" else null,
                            requiresConfirmation = true,
                            spokenConfirmationPrompt = "Kya aap $phone ko SMS bhejna chahte hain?"
                        )
                        confirmationAction = action
                        viewModel.ttsManager.speak(action.spokenConfirmationPrompt)
                    }
                },
                enabled = recipientPhone.isNotBlank() && messageText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("send_sms_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isScheduling) "Review & Schedule SMS" else "Review & Send SMS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            OutlinedButton(
                onClick = {
                    val phone = recipientPhone.ifBlank { recipientName }
                    if (phone.isNotBlank()) {
                        val action = ParsedAction(
                            actionType = ActionType.CALL_PHONE,
                            platform = "Phone Dialer",
                            recipientName = recipientName.ifBlank { phone },
                            recipientNumber = recipientPhone,
                            requiresConfirmation = true,
                            spokenConfirmationPrompt = "Kya aap $phone ko call lagana chahte hain?"
                        )
                        confirmationAction = action
                        viewModel.ttsManager.speak(action.spokenConfirmationPrompt)
                    }
                },
                enabled = recipientPhone.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("prepare_call_button")
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Call", tint = NeonGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Prepare & Initiate Call",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = NeonGreen
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
                if (action.actionType == ActionType.CALL_PHONE) {
                    viewModel.executeAction(action)
                } else if (action.scheduledEpochMillis != null) {
                    val delay = ((action.scheduledEpochMillis - System.currentTimeMillis()) / 60_000L).coerceAtLeast(1L)
                    viewModel.scheduleMessageDirect(
                        actionType = "SMS_SEND",
                        recipient = action.recipientName ?: action.recipientNumber ?: "Target",
                        content = action.messageContent ?: "",
                        delayMinutes = delay
                    )
                    viewModel.ttsManager.speak("Aapka SMS schedule kar diya gaya hai.")
                } else {
                    viewModel.sendSmsDirect(
                        phone = action.recipientNumber ?: "",
                        message = action.messageContent ?: ""
                    )
                }
            },
            onCancel = {
                confirmationAction = null
                viewModel.ttsManager.speak("SMS cancel kar diya gaya.")
            }
        )
    }
}

@Composable
fun TelegramAutomationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    var usernameOrPhone by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var aiPrompt by remember { mutableStateOf("") }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var confirmationAction by remember { mutableStateOf<ParsedAction?>(null) }

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
                    modifier = Modifier.testTag("telegram_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Telegram",
                    tint = Color(0xFF26A5E4),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Telegram Automation",
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
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TELEGRAM USERNAME OR CHAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF26A5E4), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = usernameOrPhone,
                        onValueChange = { usernameOrPhone = it },
                        label = { Text("Username or Phone") },
                        placeholder = { Text("e.g. @rahul_dev or +919876543210") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_target_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // AI Composer
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("COMPOSE MESSAGE WITH AI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonViolet, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        label = { Text("Message Intent") },
                        placeholder = { Text("e.g. Share project update and link") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("telegram_ai_prompt_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (aiPrompt.isNotBlank()) {
                                isGeneratingAi = true
                                scope.launch {
                                    messageText = viewModel.composeWithAi(aiPrompt, "Informative")
                                    isGeneratingAi = false
                                }
                            }
                        },
                        enabled = !isGeneratingAi && aiPrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isGeneratingAi) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Drafting Telegram Message...")
                        } else {
                            Text("Generate Telegram Message", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Message Editor
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
                        Text("MESSAGE CONTENT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen, letterSpacing = 1.sp)
                        OutlinedButton(
                            onClick = {
                                if (messageText.isNotBlank()) viewModel.ttsManager.speak("Telegram message: $messageText")
                            },
                            shape = RoundedCornerShape(20.dp)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("telegram_message_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Send Button
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        val action = ParsedAction(
                            actionType = ActionType.TELEGRAM_MESSAGE,
                            platform = "Telegram",
                            recipientName = usernameOrPhone.ifBlank { "Contact" },
                            messageContent = messageText,
                            requiresConfirmation = true,
                            spokenConfirmationPrompt = "Kya aap Telegram message bhejna chahte hain?"
                        )
                        confirmationAction = action
                        viewModel.ttsManager.speak(action.spokenConfirmationPrompt)
                    }
                },
                enabled = messageText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26A5E4)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("send_telegram_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Review & Open Telegram", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
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
                viewModel.sendTelegramDirect(
                    target = action.recipientName ?: "",
                    message = action.messageContent ?: ""
                )
            },
            onCancel = {
                confirmationAction = null
            }
        )
    }
}
