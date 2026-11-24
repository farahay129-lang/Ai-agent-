package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.automation.IntentAutomator
import com.example.model.ActionType
import com.example.model.ParsedAction
import com.example.ui.MainViewModel
import com.example.ui.components.ActionConfirmationDialog
import com.example.ui.theme.CyberDarkCard
import com.example.ui.theme.CyberDarkCardElevated
import com.example.ui.theme.HoloBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import kotlinx.coroutines.launch

@Composable
fun MediaControlScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var selectedMimeType by remember { mutableStateOf("image/*") }
    var captionText by remember { mutableStateOf("") }
    var visionAnalysis by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var useWhatsAppBusiness by remember { mutableStateOf(false) }
    var pendingShareAction by remember { mutableStateOf<ParsedAction?>(null) }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            selectedMimeType = context.contentResolver.getType(uri) ?: "image/*"
            visionAnalysis = ""
            viewModel.addPermittedFile(uri, selectedMimeType, "Photos")
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            selectedMimeType = context.contentResolver.getType(uri) ?: "video/*"
            visionAnalysis = ""
            viewModel.addPermittedFile(uri, selectedMimeType, "Photos")
        }
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
                    modifier = Modifier.testTag("media_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Photo & Media Control",
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
            // Camera & Gallery Quick Launchers
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CAMERA & ANDROID PHOTO PICKER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { IntentAutomator.openCamera(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = HoloBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_camera_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camera", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { IntentAutomator.openGallery(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberDarkCardElevated),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_gallery_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gallery", fontSize = 13.sp, color = NeonCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_photo_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Photo", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_video_button")
                        ) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Video", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Selected Media Preview & Gemini Vision Analysis
            if (selectedMediaUri != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SELECTED MEDIA",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (selectedMimeType.startsWith("image")) {
                            AsyncImage(
                                model = selectedMediaUri,
                                contentDescription = "Selected Photo Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    val uri = selectedMediaUri ?: return@Button
                                    isAnalyzing = true
                                    scope.launch {
                                        val bitmap: Bitmap? = try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                                val src = ImageDecoder.createSource(context.contentResolver, uri)
                                                ImageDecoder.decodeBitmap(src) { decoder, info, _ ->
                                                    val maxDim = maxOf(info.size.width, info.size.height)
                                                    if (maxDim > 1024) {
                                                        decoder.setTargetSampleSize((maxDim / 1024).coerceAtLeast(1))
                                                    }
                                                }
                                            } else {
                                                @Suppress("DEPRECATION")
                                                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                            }
                                        } catch (e: Exception) {
                                            null
                                        }

                                        if (bitmap != null) {
                                            val result = viewModel.analyzeImageWithJarvis(
                                                bitmap,
                                                "Describe this photo briefly and write a great WhatsApp/Instagram caption."
                                            )
                                            visionAnalysis = result
                                            if (captionText.isBlank()) captionText = result
                                            viewModel.ttsManager.speak(result)
                                        } else {
                                            visionAnalysis = "Unable to decode selected image."
                                        }
                                        isAnalyzing = false
                                    }
                                },
                                enabled = !isAnalyzing,
                                colors = ButtonDefaults.buttonColors(containerColor = HoloBlue),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isAnalyzing) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("JARVIS Vision analyzing...")
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Analyze & Caption with Gemini AI", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Text("Video / Media ready: ${selectedMediaUri?.lastPathSegment}", color = NeonCyan, fontSize = 13.sp)
                            }
                        }

                        if (visionAnalysis.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(visionAnalysis, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = captionText,
                            onValueChange = { captionText = it },
                            label = { Text("Caption / Message") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target WhatsApp Business", fontSize = 13.sp)
                            Switch(
                                checked = useWhatsAppBusiness,
                                onCheckedChange = { useWhatsAppBusiness = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    pendingShareAction = ParsedAction(
                                        actionType = ActionType.WHATSAPP_MESSAGE,
                                        platform = if (useWhatsAppBusiness) "WhatsApp Business" else "WhatsApp Media",
                                        messageContent = captionText.ifBlank { "Sending selected media" },
                                        requiresConfirmation = true,
                                        spokenConfirmationPrompt = "Kya aap yeh media WhatsApp par send karna chahte hain?"
                                    )
                                    viewModel.ttsManager.speak("Kya aap yeh media WhatsApp par send karna chahte hain?")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("send_media_whatsapp_button")
                            ) {
                                Text("Send via WhatsApp", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    selectedMediaUri?.let { uri ->
                                        viewModel.shareMediaGeneric(uri, selectedMimeType, captionText)
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share App", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pick a photo or video using Android Photo Picker above. Zero broad storage permissions required.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (pendingShareAction != null) {
        ActionConfirmationDialog(
            action = pendingShareAction!!,
            onConfirm = {
                pendingShareAction = null
                selectedMediaUri?.let { uri ->
                    viewModel.shareMediaToWhatsApp(uri, captionText, selectedMimeType, useWhatsAppBusiness)
                }
            },
            onCancel = { pendingShareAction = null }
        )
    }
}
