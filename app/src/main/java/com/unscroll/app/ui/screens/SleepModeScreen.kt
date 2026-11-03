package com.unscroll.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unscroll.app.data.model.SleepScheduleConfig
import com.unscroll.app.ui.theme.*
import com.unscroll.app.ui.viewmodel.UnscrollUiState

@Composable
fun SleepModeScreen(
    uiState: UnscrollUiState,
    onUpdateSleepConfig: (SleepScheduleConfig) -> Unit
) {
    val config = uiState.sleepConfig
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sleep Mode",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Circadian protection against late-night scrolling",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            Switch(
                checked = config.isEnabled,
                onCheckedChange = { onUpdateSleepConfig(config.copy(isEnabled = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkBackground,
                    checkedTrackColor = SoftGreenPrimary,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = CharcoalSurfaceElevated
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bedtime Schedule Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CIRCADIAN SCHEDULE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Bedtime
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Bedtime",
                                tint = SoftGreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bedtime",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%02d:%02d", config.bedtimeHour, config.bedtimeMinute),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = TextPrimary
                            )
                        )
                    }

                    // Wake Up
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Wake Up",
                                tint = AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Wake Up",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format("%02d:%02d", config.wakeHour, config.wakeMinute),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = TextPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = CharcoalSurfaceBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Bedtime Hour Adjuster
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bedtime: ${config.bedtimeHour}:00",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp)
                    )
                }
                Slider(
                    value = config.bedtimeHour.toFloat(),
                    onValueChange = { onUpdateSleepConfig(config.copy(bedtimeHour = it.toInt())) },
                    valueRange = 18f..24f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = SoftGreenPrimary,
                        activeTrackColor = SoftGreenPrimary,
                        inactiveTrackColor = CharcoalSurfaceElevated
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Wake Hour Adjuster
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wake-Up: ${config.wakeHour}:00",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp)
                    )
                }
                Slider(
                    value = config.wakeHour.toFloat(),
                    onValueChange = { onUpdateSleepConfig(config.copy(wakeHour = it.toInt())) },
                    valueRange = 5f..11f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberWarning,
                        activeTrackColor = AmberWarning,
                        inactiveTrackColor = CharcoalSurfaceElevated
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Night Restrictions
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "OVERNIGHT RULES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Strict social media lockdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lock Social Media Overnight",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp)
                        )
                        Text(
                            text = "Schedule preference is saved, but automatic app blocking is not implemented yet.",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                        )
                    }
                    Switch(
                        checked = config.blockAllTargetApps,
                        onCheckedChange = { onUpdateSleepConfig(config.copy(blockAllTargetApps = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBackground,
                            checkedTrackColor = SoftGreenPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CharcoalSurfaceBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Grayscale mode trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Simulate Grayscale Bedtime",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp)
                        )
                        Text(
                            text = "Grayscale is not applied automatically yet; this setting currently saves your preference.",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                        )
                    }
                    Switch(
                        checked = config.enableGrayscaleBedtime,
                        onCheckedChange = { onUpdateSleepConfig(config.copy(enableGrayscaleBedtime = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBackground,
                            checkedTrackColor = SoftGreenPrimary
                        )
                    )
                }
            }
        }
    }
}
