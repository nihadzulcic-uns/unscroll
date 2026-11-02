package com.unscroll.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unscroll.app.ui.theme.*
import com.unscroll.app.ui.viewmodel.UnscrollUiState

@Composable
fun FocusModeScreen(
    uiState: UnscrollUiState,
    onStartFocus: (Int) -> Unit,
    onPauseFocus: () -> Unit,
    onCancelFocus: () -> Unit
) {
    var selectedPresetMinutes by remember { mutableStateOf(25) }
    var customMinutes by remember { mutableStateOf(30f) }
    val isSessionActive = uiState.focusSession.isActive
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Focus Mode",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = if (isSessionActive) "Session in progress" else "Carve out intentional deep work",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Circular Timer Gauge (Mobile optimized 210dp)
        Box(
            modifier = Modifier.size(210.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = if (isSessionActive && uiState.focusSession.totalSeconds > 0) {
                uiState.focusSession.remainingSeconds.toFloat() / uiState.focusSession.totalSeconds.toFloat()
            } else {
                1f
            }

            // Animated breathing pulse when active
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = if (isSessionActive && !uiState.focusSession.isPaused) 1.04f else 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = EaseInOutQuad),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breathing"
            )

            Canvas(modifier = Modifier.size(190.dp * pulseScale)) {
                // Background Track
                drawArc(
                    color = CharcoalSurfaceElevated,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active Progress Arc
                drawArc(
                    color = SoftGreenPrimary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isSessionActive) {
                    val remainingMins = uiState.focusSession.remainingSeconds / 60
                    val remainingSecs = uiState.focusSession.remainingSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", remainingMins, remainingSecs),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = if (uiState.focusSession.isPaused) "PAUSED" else "DEEP PRESENCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontSize = 10.sp,
                            color = if (uiState.focusSession.isPaused) AmberWarning else SoftGreenPrimary
                        )
                    )
                } else {
                    Text(
                        text = "${selectedPresetMinutes}m",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = "READY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.5.sp,
                            fontSize = 10.sp,
                            color = SoftGreenPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isSessionActive) {
            // Active Session Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPauseFocus,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = CharcoalSurface,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.focusSession.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "Toggle Pause",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (uiState.focusSession.isPaused) "Resume" else "Pause", fontSize = 13.sp)
                }

                Button(
                    onClick = onCancelFocus,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDestructive)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "End Session", color = Color.White, fontSize = 13.sp)
                }
            }
        } else {
            // Customizable Session Duration selector
            Text(
                text = "CHOOSE SESSION DURATION",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(15, 25, 45, 60).forEach { mins ->
                    val isSelected = selectedPresetMinutes == mins
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) SoftGreenPrimary else CharcoalSurface)
                            .clickable { selectedPresetMinutes = mins },
                        color = Color.Transparent,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder) else null
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) DarkBackground else TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Custom Duration",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Text(
                    text = "${customMinutes.toInt()} minutes",
                    style = MaterialTheme.typography.bodySmall.copy(color = SoftGreenPrimary, fontWeight = FontWeight.Bold)
                )
            }
            Slider(
                value = customMinutes,
                onValueChange = {
                    customMinutes = it
                    selectedPresetMinutes = it.toInt()
                },
                valueRange = 5f..120f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = SoftGreenPrimary,
                    activeTrackColor = SoftGreenPrimary,
                    inactiveTrackColor = CharcoalSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Start Button
            Button(
                onClick = { onStartFocus(selectedPresetMinutes) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftGreenPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start",
                    tint = DarkBackground,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Begin ${selectedPresetMinutes}-Minute Session",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mindful insight card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Mindfulness Tip",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = SoftGreenSubtle,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Whenever an impulse to scroll occurs, take 3 deep belly breaths. The urge to doomscroll typically dissolves within 90 seconds.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                )
            }
        }
    }
}
