package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
fun ScreenAutomationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current

    val isA11yActive by HandsFreeAccessibilityService.isServiceActive.collectAsState()
    val activePackage by HandsFreeAccessibilityService.lastActivePackage.collectAsState()
    val visibleButtons by HandsFreeAccessibilityService.visibleButtons.collectAsState()
    val screenText by HandsFreeAccessibilityService.visibleScreenText.collectAsState()

    var tapLabelInput by remember { mutableStateOf("") }
    var typeTextInput by remember { mutableStateOf("") }
    var statusFeedback by remember { mutableStateOf("") }

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
                    modifier = Modifier.testTag("screen_auto_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.TouchApp, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Smart Screen Control",
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
            // Accessibility Engine Status Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isA11yActive) NeonGreen else NeonAmber)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isA11yActive) "ACCESSIBILITY ENGINE ONLINE" else "ACCESSIBILITY PERMISSION NEEDED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isA11yActive) NeonGreen else NeonAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isA11yActive) {
                            "Active Window: ${activePackage.ifBlank { "JARVIS HUD" }}"
                        } else {
                            "Enable JARVIS Accessibility Service in Android Settings to automate scrolling, button taps, form typing, and screen reading. Password & OTP fields are always strictly blocked for your security."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isA11yActive) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HoloBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("enable_a11y_button")
                        ) {
                            Text("Open Accessibility Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Global Navigation & Scroll Pad
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HANDS-FREE NAVIGATION & SCROLL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerScreenScroll(forward = true)
                                statusFeedback = "Triggered Scroll Down"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "Scroll Down", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scroll Down", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.triggerScreenScroll(forward = false)
                                statusFeedback = "Triggered Scroll Up"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Scroll Up", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scroll Up", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerScreenBack()
                                statusFeedback = "Navigated Back"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.triggerScreenHome()
                                statusFeedback = "Returned Home"
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Go Home", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Home / Close", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Voice-to-Touch: Tap Button & Type Into Form
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VOICE-TO-TOUCH & FORM FILLING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tapLabelInput,
                        onValueChange = { tapLabelInput = it },
                        label = { Text("Button Label to Tap") },
                        placeholder = { Text("e.g. Send, Next, Submit") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (tapLabelInput.isNotBlank()) {
                                viewModel.triggerScreenTap(tapLabelInput)
                                statusFeedback = "Attempted tap on '$tapLabelInput'"
                            }
                        },
                        enabled = tapLabelInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = HoloBlue),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tap Matching Button", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = typeTextInput,
                        onValueChange = { typeTextInput = it },
                        label = { Text("Text to Type in Focused Field") },
                        placeholder = { Text("Non-password fields only") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (typeTextInput.isNotBlank()) {
                                viewModel.triggerScreenType(typeTextInput)
                                statusFeedback = "Typed text into focused field"
                            }
                        },
                        enabled = typeTextInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fill Active Input", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Live Screen Reader & Recognized Buttons
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SCREEN INSPECTOR (SAFE MODE)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonGreen)
                        }
                        OutlinedButton(
                            onClick = {
                                val summary = HandsFreeAccessibilityService.readCurrentScreenSummary()
                                statusFeedback = summary
                                viewModel.ttsManager.speak(summary)
                            },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Read Screen", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Read Aloud", fontSize = 11.sp)
                        }
                    }

                    if (visibleButtons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Detected Buttons:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(visibleButtons) { btnLabel ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = CyberDarkCard,
                                    modifier = Modifier.clickable {
                                        tapLabelInput = btnLabel
                                        viewModel.triggerScreenTap(btnLabel)
                                    }
                                ) {
                                    Text(
                                        text = btnLabel,
                                        fontSize = 11.sp,
                                        color = NeonCyan,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (screenText.isNotBlank() || statusFeedback.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = statusFeedback.ifBlank { screenText },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Immediate Stop Control
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.emergencyStopAll() }
                    .testTag("screen_auto_stop_button"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF87171))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("STOP SCREEN AUTOMATION IMMEDIATELY", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFCA5A5))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
