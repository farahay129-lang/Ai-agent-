package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AssistantLanguage
import com.example.service.AssistantNotificationListenerService
import com.example.service.HandsFreeAccessibilityService
import com.example.ui.MainViewModel
import com.example.ui.theme.CyberDarkCard
import com.example.ui.theme.CyberDarkCardElevated
import com.example.ui.theme.HoloBlue
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink

@Composable
fun PermissionsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var hasSmsPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
    }
    var hasContactsPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }
    var hasCallPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
    }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var hasNotificationsPermission by remember {
        mutableStateOf(
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val isA11yActive by HandsFreeAccessibilityService.isServiceActive.collectAsState()
    val isNotificationListenerActive by AssistantNotificationListenerService.isListenerConnected.collectAsState()

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasMicPermission = it }
    val smsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasSmsPermission = it }
    val contactsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasContactsPermission = it
        if (it) viewModel.refreshContacts("")
    }
    val callLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCallPermission = it }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasCameraPermission = it }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasNotificationsPermission = it }

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
                    modifier = Modifier.testTag("permissions_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Permissions & Security", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "You have complete control over every JARVIS permission. JARVIS never bypasses Android security, never reads banking passwords/OTPs, and never silently records calls.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PermissionTile(
                title = "Microphone (Voice & Wake Word)",
                description = "Required for Hindi/English voice commands and \"Hey JARVIS\" activation.",
                icon = Icons.Default.Mic,
                isGranted = hasMicPermission,
                onRequest = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )

            PermissionTile(
                title = "Contacts (Search & Messaging)",
                description = "Allows looking up contact phone numbers when you say 'Rahul ko message bhejo'.",
                icon = Icons.Default.People,
                isGranted = hasContactsPermission,
                onRequest = { contactsLauncher.launch(Manifest.permission.READ_CONTACTS) }
            )

            PermissionTile(
                title = "Send SMS",
                description = "Allows dispatching SMS messages after your explicit confirmation.",
                icon = Icons.Default.Sms,
                isGranted = hasSmsPermission,
                onRequest = { smsLauncher.launch(Manifest.permission.SEND_SMS) }
            )

            PermissionTile(
                title = "Phone Calls",
                description = "Allows preparing and placing calls after confirmation (otherwise opens Dialer).",
                icon = Icons.Default.Phone,
                isGranted = hasCallPermission,
                onRequest = { callLauncher.launch(Manifest.permission.CALL_PHONE) }
            )

            PermissionTile(
                title = "Camera",
                description = "Optional permission for direct camera actions; Photo Picker requires zero storage permission.",
                icon = Icons.Default.CameraAlt,
                isGranted = hasCameraPermission,
                onRequest = { cameraLauncher.launch(Manifest.permission.CAMERA) }
            )

            PermissionTile(
                title = "Notifications",
                description = "Required for JARVIS Always-On Foreground Service status and scheduled task alerts.",
                icon = Icons.Default.Notifications,
                isGranted = hasNotificationsPermission,
                onRequest = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )

            SpecialAccessTile(
                title = "JARVIS Accessibility Service",
                description = "Enables screen scrolling, button tapping, form typing, and non-sensitive screen reading.",
                isActive = isA11yActive,
                onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            )

            SpecialAccessTile(
                title = "Notification Listener Access",
                description = "Allows JARVIS to read incoming WhatsApp/SMS notifications for smart replies.",
                isActive = isNotificationListenerActive,
                onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                }
            )

            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    })
                },
                modifier = Modifier.fillMaxWidth().testTag("open_app_settings_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Manage App Permissions in Android Settings", color = NeonCyan)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PermissionTile(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(icon, contentDescription = null, tint = if (isGranted) NeonGreen else NeonPink, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = if (isGranted) "GRANTED" else "REQUIRED",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isGranted) NeonGreen else NeonPink
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = HoloBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Grant Permission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SpecialAccessTile(
    title: String,
    description: String,
    isActive: Boolean,
    onOpenSettings: () -> Unit
) {
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
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = if (isActive) "ACTIVE" else "DISABLED",
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = if (isActive) NeonGreen else NeonAmber
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(onClick = onOpenSettings, shape = RoundedCornerShape(8.dp)) {
                Text(if (isActive) "Configure in Settings" else "Enable in Android Settings", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val currentLang by viewModel.currentLanguage.collectAsState()
    val isWakeWordOn by viewModel.isWakeWordEnabled.collectAsState()
    val preferences by viewModel.userPreferences.collectAsState()

    var speechRate by remember { mutableFloatStateOf(0.95f) }
    var speechPitch by remember { mutableFloatStateOf(1.0f) }
    var newPrefKey by remember { mutableStateOf("") }
    var newPrefValue by remember { mutableStateOf("") }

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
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.Settings, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("JARVIS Settings & Memory", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
            // Language Preference Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("VOICE LANGUAGE (भाषा)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    AssistantLanguage.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setLanguage(lang) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.displayName,
                                fontSize = 14.sp,
                                fontWeight = if (currentLang == lang) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentLang == lang) NeonCyan else MaterialTheme.colorScheme.onSurface
                            )
                            if (currentLang == lang) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = NeonCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Always-On Foreground Service Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Always-On \"Hey JARVIS\" Wake Word", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Runs foreground microphone service with persistent notification", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = isWakeWordOn,
                        onCheckedChange = { viewModel.toggleWakeWordService() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonCyan)
                    )
                }
            }

            // User-Approved Preferences Memory
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("APPROVED JARVIS MEMORY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "JARVIS only remembers preferences you explicitly approve.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPrefKey,
                            onValueChange = { newPrefKey = it },
                            label = { Text("Key (e.g. Name)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = newPrefValue,
                            onValueChange = { newPrefValue = it },
                            label = { Text("Value") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        IconButton(
                            onClick = {
                                if (newPrefKey.isNotBlank() && newPrefValue.isNotBlank()) {
                                    viewModel.saveUserPreference(newPrefKey, newPrefValue)
                                    newPrefKey = ""
                                    newPrefValue = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Save preference", tint = NeonCyan)
                        }
                    }

                    if (preferences.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        preferences.forEach { pref ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${pref.prefKey}: ${pref.prefValue}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                IconButton(onClick = { viewModel.deleteUserPreference(pref.prefKey) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Forget preference", tint = NeonPink, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Text to Speech Tuning
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = HoloBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TTS VOICE CALIBRATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = HoloBlue, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Speech Rate (${String.format("%.2f", speechRate)}x):", fontSize = 13.sp)
                    Slider(
                        value = speechRate,
                        onValueChange = {
                            speechRate = it
                            viewModel.ttsManager.setSpeechRate(it)
                        },
                        valueRange = 0.75f..1.4f,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                    )
                    Text("Speech Pitch (${String.format("%.2f", speechPitch)}x):", fontSize = 13.sp)
                    Slider(
                        value = speechPitch,
                        onValueChange = {
                            speechPitch = it
                            viewModel.ttsManager.setPitch(it)
                        },
                        valueRange = 0.8f..1.3f,
                        colors = SliderDefaults.colors(thumbColor = HoloBlue, activeTrackColor = HoloBlue)
                    )
                    Button(
                        onClick = {
                            viewModel.ttsManager.speak("Namaste Sir, JARVIS voice calibration complete.", currentLang)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberDarkCardElevated),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Test JARVIS Voice", color = NeonCyan, fontSize = 12.sp)
                    }
                }
            }

            // Security Architecture Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("JARVIS SECURITY & ZERO-TRUST POLICY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Brain: Google Gemini 3.8 Flash via Firebase AI Logic + App Check\n• Privacy: Password, PIN & OTP screen nodes are strictly redacted\n• Confirmations: Mandatory prompt before messages, calls, or media shares\n• Storage: Local encrypted Room DB + zero-permission Photo Picker",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            // Emergency Stop Tile
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.emergencyStopAll() }
                    .testTag("settings_emergency_stop"),
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
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EMERGENCY: HALT ALL JARVIS OPERATIONS", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
