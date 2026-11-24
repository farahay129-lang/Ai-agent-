package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ParsedAction
import com.example.ui.theme.CyberDarkCardElevated
import com.example.ui.theme.HoloBlue
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink

@Composable
fun GlowingMicButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Int = 82
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.28f else 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 550 else 1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size((sizeDp * 1.32f).dp)
    ) {
        Box(
            modifier = Modifier
                .size((sizeDp * pulseScale).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            if (isListening) NeonPink.copy(alpha = 0.45f) else NeonCyan.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .minimumInteractiveComponentSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        if (isListening) {
                            listOf(NeonPink, Color(0xFFFF6B81))
                        } else {
                            listOf(HoloBlue, NeonCyan)
                        }
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.9f), NeonCyan)
                    ),
                    shape = CircleShape
                )
                .clickable(onClick = onClick)
                .testTag("mic_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop listening" else "Activate JARVIS voice",
                tint = Color.White,
                modifier = Modifier.size((sizeDp * 0.46f).dp)
            )
        }
    }
}

@Composable
fun ActionConfirmationDialog(
    action: ParsedAction,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "JARVIS Security Confirmation",
                        tint = NeonAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "JARVIS Authorization",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = action.spokenConfirmationPrompt.ifBlank {
                        "Confirm this automated action before execution?"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkCardElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Module", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(action.platform, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NeonCyan)
                        }

                        if (!action.recipientName.isNullOrBlank() || !action.recipientNumber.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Recipient", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = listOfNotNull(action.recipientName, action.recipientNumber).distinct().joinToString(" • "),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (!action.scheduledTimeString.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Schedule", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(action.scheduledTimeString, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NeonAmber)
                            }
                        }

                        if (action.planSteps.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Execution Plan:", fontSize = 12.sp, color = NeonCyan)
                            action.planSteps.forEachIndexed { idx, step ->
                                Text("${idx + 1}. $step", fontSize = 12.sp, color = Color.White)
                            }
                        }

                        if (!action.messageContent.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Payload / Content:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(action.messageContent, fontSize = 13.sp, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Security Policy: JARVIS never sends messages, uploads photos, or places calls without your approval.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_action_button")
            ) {
                Icon(Icons.Default.Done, contentDescription = "Approve", tint = Color.Black, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Approve", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("cancel_action_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancel")
            }
        }
    )
}
