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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import kotlinx.coroutines.launch

@Composable
fun SocialAutomationScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val platforms = listOf("Instagram", "X (Twitter)", "Facebook")

    var topicPrompt by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf("Viral") }
    var generatedCaption by remember { mutableStateOf("") }
    var generatedHashtags by remember { mutableStateOf<List<String>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }

    var confirmationAction by remember { mutableStateOf<ParsedAction?>(null) }

    val tones = listOf("Viral", "Inspirational", "Humorous", "Promotional", "Educational")

    val activePlatform = platforms[selectedTabIndex]

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
                    modifier = Modifier.testTag("social_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Social Automation",
                    tint = NeonPink,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Social Media Suite",
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
            // Platform Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CyberDarkCard,
                contentColor = NeonCyan
            ) {
                platforms.forEachIndexed { index, platform ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = platform,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("social_tab_$platform")
                    )
                }
            }

            // Post Concept & Topic
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI CAPTION & HASHTAG GENERATOR",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonViolet,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = topicPrompt,
                        onValueChange = { topicPrompt = it },
                        label = { Text("What is your post about?") },
                        placeholder = { Text("e.g. Launched a new tech app today, grateful for team support") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("social_topic_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Style / Mood:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(tones) { tone ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (selectedTone == tone) NeonPink.copy(alpha = 0.25f) else CyberDarkCardElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedTone == tone) NeonPink else Color.Transparent
                                ),
                                modifier = Modifier.clickable { selectedTone = tone }
                            ) {
                                Text(
                                    text = tone,
                                    fontSize = 11.sp,
                                    color = if (selectedTone == tone) NeonPink else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (topicPrompt.isNotBlank()) {
                                isGenerating = true
                                scope.launch {
                                    val result = viewModel.generateSocialPostWithAi(
                                        topic = topicPrompt,
                                        platform = activePlatform,
                                        tone = selectedTone
                                    )
                                    generatedCaption = result.caption
                                    generatedHashtags = result.hashtags
                                    isGenerating = false
                                }
                            }
                        },
                        enabled = !isGenerating && topicPrompt.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_social_post_button")
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini writing caption...")
                        } else {
                            Text("Generate Captions & Hashtags", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Generated Preview & Editor
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberDarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PREPARED POST PREVIEW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = generatedCaption,
                        onValueChange = { generatedCaption = it },
                        label = { Text("Caption") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("social_caption_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (generatedHashtags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Hashtags:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(generatedHashtags) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = NeonCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        color = NeonCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Publish Button with Confirmation
            val fullPostText = buildString {
                append(generatedCaption)
                if (generatedHashtags.isNotEmpty()) {
                    append("\n\n")
                    append(generatedHashtags.joinToString(" "))
                }
            }

            Button(
                onClick = {
                    if (fullPostText.isNotBlank()) {
                        val actionType = when (selectedTabIndex) {
                            0 -> ActionType.INSTAGRAM_POST
                            1 -> ActionType.X_POST
                            else -> ActionType.FACEBOOK_POST
                        }
                        val action = ParsedAction(
                            actionType = actionType,
                            platform = activePlatform,
                            messageContent = fullPostText,
                            requiresConfirmation = true,
                            spokenConfirmationPrompt = "Kya aap $activePlatform par post karna chahte hain?"
                        )
                        confirmationAction = action
                        viewModel.ttsManager.speak(action.spokenConfirmationPrompt)
                    }
                },
                enabled = generatedCaption.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_social_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm & Post to $activePlatform",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
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
                viewModel.openSocialPost(
                    platform = if (selectedTabIndex == 0) "instagram" else if (selectedTabIndex == 1) "x" else "facebook",
                    content = action.messageContent ?: ""
                )
            },
            onCancel = {
                confirmationAction = null
            }
        )
    }
}
