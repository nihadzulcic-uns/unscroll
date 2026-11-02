package com.unscroll.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unscroll.app.data.model.AppProtectionConfig
import com.unscroll.app.data.model.ProtectionMode
import com.unscroll.app.data.model.ServiceStatus
import com.unscroll.app.ui.theme.*
import com.unscroll.app.ui.viewmodel.UnscrollUiState

@Composable
fun ProtectionScreen(
    uiState: UnscrollUiState,
    onSetProtectionMode: (ProtectionMode) -> Unit,
    onUpdateInstagram: (AppProtectionConfig) -> Unit,
    onUpdateYouTube: (AppProtectionConfig) -> Unit,
    onUpdateTikTok: (AppProtectionConfig) -> Unit,
    onSimulateInterception: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = "Protection Shield",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
        )
        Text(
            text = "Real-time algorithmic short-form video containment",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Operating Mode Selector (Segmented)
        Text(
            text = "DETECTION MODE",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalSurface, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            listOf(
                ProtectionMode.AUTOMATIC to "Auto Sentry",
                ProtectionMode.MANUAL to "Manual Limit",
                ProtectionMode.STRICT to "Strict Shield"
            ).forEach { (mode, label) ->
                val isSelected = uiState.protectionMode == mode
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSetProtectionMode(mode) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) CharcoalSurfaceElevated else Color.Transparent,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, SoftGreenPrimary) else null
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SoftGreenPrimary else TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = when (uiState.protectionMode) {
                ProtectionMode.AUTOMATIC -> "Auto: Senses Reels & Shorts in active window and presents a calm breath barrier."
                ProtectionMode.MANUAL -> "Manual: Allows viewing until daily limit is reached."
                ProtectionMode.STRICT -> "Strict: Hard block without skip options during focus sessions."
            },
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Target Apps Settings
        Text(
            text = "APP-SPECIFIC CONTROLS",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Instagram Card
        AppProtectionSettingCard(
            title = "Instagram",
            subtitle = "Reels & Explore Filter",
            config = uiState.instagramConfig,
            onConfigChange = onUpdateInstagram,
            onOpenAccessibilitySettings = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onOpenOverlaySettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // YouTube Card
        AppProtectionSettingCard(
            title = "YouTube",
            subtitle = "Shorts Loop Disruptor",
            config = uiState.youtubeConfig,
            onConfigChange = onUpdateYouTube,
            onOpenAccessibilitySettings = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onOpenOverlaySettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // TikTok Card
        AppProtectionSettingCard(
            title = "TikTok",
            subtitle = "Feed Friction Barrier",
            config = uiState.tiktokConfig,
            onConfigChange = onUpdateTikTok,
            onOpenAccessibilitySettings = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            onOpenOverlaySettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Security & Privacy Disclaimer Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Zero-Password Privacy",
                    tint = SoftGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Privacy-conscious feed detection",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Feed detection runs on your device by checking selected interface identifiers and descriptions. Recognized password and text-input fields are skipped. The service may access parts of the active app interface to identify supported feeds.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AppProtectionSettingCard(
    title: String,
    subtitle: String,
    config: AppProtectionConfig,
    onConfigChange: (AppProtectionConfig) -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CharcoalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }

                // Verified Status Badge & Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when (config.status) {
                                        ServiceStatus.ACTIVE -> SoftGreenPrimary
                                        ServiceStatus.PERMISSION_REQUIRED -> AmberWarning
                                        ServiceStatus.NOT_CONFIGURED -> TextMuted
                                        ServiceStatus.ERROR -> RoseDestructive
                                    }
                                )
                        )
                        Text(
                            text = config.status.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = when (config.status) {
                                    ServiceStatus.ACTIVE -> SoftGreenPrimary
                                    ServiceStatus.PERMISSION_REQUIRED -> AmberWarning
                                    ServiceStatus.NOT_CONFIGURED -> TextMuted
                                    ServiceStatus.ERROR -> RoseDestructive
                                }
                            )
                        )
                    }

                    Switch(
                        checked = config.isProtectionEnabled,
                        onCheckedChange = { onConfigChange(config.copy(isProtectionEnabled = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBackground,
                            checkedTrackColor = SoftGreenPrimary,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = CharcoalSurfaceElevated
                        )
                    )
                }
            }

            // Warning Banner if user enabled protection but system permission is missing
            if (config.isProtectionEnabled && config.status == ServiceStatus.PERMISSION_REQUIRED) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = AmberWarning.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Permission Required",
                            style = MaterialTheme.typography.labelSmall.copy(color = AmberWarning, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Enable UNSCROLL Accessibility Service and Display Over Other Apps permission to activate.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onOpenAccessibilitySettings,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(text = "Accessibility", fontSize = 10.sp, color = AmberWarning)
                            }
                            OutlinedButton(
                                onClick = onOpenOverlaySettings,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(text = "Overlay Perm", fontSize = 10.sp, color = AmberWarning)
                            }
                        }
                    }
                }
            }

            if (config.isProtectionEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CharcoalSurfaceBorder)
                Spacer(modifier = Modifier.height(12.dp))

                // Short-form only toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Target Short-Form Feeds Only",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp)
                        )
                        Text(
                            text = "Blocks Reels/Shorts; keeps direct messages and search available.",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                        )
                    }
                    Checkbox(
                        checked = config.blockShortFormOnly,
                        onCheckedChange = { onConfigChange(config.copy(blockShortFormOnly = it)) },
                        colors = CheckboxDefaults.colors(checkedColor = SoftGreenPrimary)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Daily Limit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Quota: ${config.dailyLimitMinutes} minutes",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp)
                    )
                }
                Slider(
                    value = config.dailyLimitMinutes.toFloat(),
                    onValueChange = { onConfigChange(config.copy(dailyLimitMinutes = it.toInt())) },
                    valueRange = 5f..90f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = SoftGreenPrimary,
                        activeTrackColor = SoftGreenPrimary,
                        inactiveTrackColor = CharcoalSurfaceElevated
                    )
                )
            }
        }
    }
}
