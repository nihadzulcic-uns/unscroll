package com.unscroll.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unscroll.app.ui.theme.*
import com.unscroll.app.ui.viewmodel.UnscrollUiState

@Composable
fun SettingsScreen(
    uiState: UnscrollUiState,
    onSetLanguage: (String) -> Unit
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
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
        )
        Text(
            text = "Preferences, privacy controls & system permissions",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Language Selection
        Text(
            text = "LANGUAGE",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val languages = listOf(
                    "en" to "English (US)",
                    "de" to "Deutsch",
                    "fr" to "Français",
                    "es" to "Español"
                )

                languages.forEachIndexed { index, (code, name) ->
                    val isSelected = uiState.currentLanguage == code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSetLanguage(code) }
                            .padding(vertical = 9.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SoftGreenPrimary else TextPrimary,
                                fontSize = 13.sp
                            )
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = SoftGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (index < languages.size - 1) {
                        HorizontalDivider(color = CharcoalSurfaceBorder)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Privacy & Security Section
        Text(
            text = "PRIVACY & PERMISSIONS",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Privacy Shield",
                        tint = SoftGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "On-Device Privacy",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Feed detection runs locally on your device. The current app build does not request Internet access. The accessibility service checks selected interface view IDs locally to detect supported feed containers. It does not read on-screen text or accessibility descriptions.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CharcoalSurfaceBorder)
                Spacer(modifier = Modifier.height(10.dp))

                // Accessibility Settings Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Accessibility Service",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp)
                        )
                        Text(
                            text = "Required for Reels & Shorts container detection",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = CharcoalSurfaceBorder)
                Spacer(modifier = Modifier.height(8.dp))

                // Usage Access Settings Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Usage Stats Access",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp)
                        )
                        Text(
                            text = "Required for verified on-device screen time metrics",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Open Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Implemented vs Planned Roadmap
        Text(
            text = "FEATURE ARCHITECTURE STATUS",
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Implemented in v1.0 (Active)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = SoftGreenPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                listOf(
                    "✓ Jetpack Compose & Material 3 Dark Architecture",
                    "✓ On-Device Reels, Shorts & TikTok Detection Service",
                    "✓ Recognized Password and Text-Input Fields Are Skipped",
                    "✓ Mindful Interception & Breathing Overlay Shield",
                    "✓ Focus Mode Timer with Custom Durations",
                    "✓ Circadian Sleep Mode & Overnight App Lockout",
                    "✓ UsageStatsManager Screen Time Analytics",
                    "✓ Verified Service Status (Not configured, Permission required, Active, Error)",
                    "✓ Local SharedPreferences Persistence"
                ).forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp),
                        modifier = Modifier.padding(vertical = 1.5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = CharcoalSurfaceBorder)
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Planned in Roadmap (v1.1+)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = AmberWarning,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                listOf(
                    "○ WearOS Quick Focus Mode tile & companion sync",
                    "○ Android Glance Home Screen wellbeing widgets",
                    "○ Micro-haptic tactile feedback for scroll friction",
                    "○ End-to-end encrypted backup of habit records"
                ).forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted, fontSize = 11.sp),
                        modifier = Modifier.padding(vertical = 1.5.dp)
                    )
                }
            }
        }
    }
}
