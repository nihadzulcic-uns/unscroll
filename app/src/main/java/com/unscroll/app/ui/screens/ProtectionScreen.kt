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
    val consentPrefs = remember {
        context.getSharedPreferences("unscroll_privacy_consent", android.content.Context.MODE_PRIVATE)
    }
    var hasAccessibilityConsent by remember {
        mutableStateOf(consentPrefs.getBoolean("accessibility_disclosure_v2_accepted", false))
    }
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }
    val openAccessibilitySettings = {
        if (hasAccessibilityConsent) {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        } else {
            showAccessibilityDisclosure = true
        }
    }

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

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SoftGreenPrimary.copy(alpha = 0.55f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "AUTOMATIC DETECTION — EXPERIMENTAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGreenPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Manual time limits and strict blocking are not implemented yet, so they are hidden rather than shown as working controls.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Current prototype: automatic, experimental Reels and Shorts view-ID detection with a dismissible mindful pause. It does not lock the app or enforce a daily quota.",
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
            subtitle = "Instagram Reels detection (experimental)",
            config = uiState.instagramConfig,
            onConfigChange = onUpdateInstagram,
            onOpenAccessibilitySettings = openAccessibilitySettings,
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
            subtitle = "YouTube Shorts detection (experimental)",
            config = uiState.youtubeConfig,
            onConfigChange = onUpdateYouTube,
            onOpenAccessibilitySettings = openAccessibilitySettings,
            onOpenOverlaySettings = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                        text = "Feed detection runs locally by checking selected view IDs inside Instagram and YouTube. To detect when you leave either app, the service also receives window events from other apps but reads only their package identifier; it does not inspect their screens. It does not read screen text, messages, accessibility descriptions, keystrokes, or passwords. No accessibility data is sent to an UNSCROLL server. TikTok detection is currently unavailable.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                    )
                }
            }
        }
    }

    if (showAccessibilityDisclosure) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDisclosure = false; hasAccessibilityConsent = consentPrefs.getBoolean("accessibility_disclosure_v2_accepted", false) },
            title = { Text("How UNSCROLL uses Accessibility") },
            text = {
                Column {
                    Text(
                        "UNSCROLL uses Android Accessibility access to identify selected interface view IDs inside Instagram Reels and YouTube Shorts, so it can show a mindful pause overlay. It also receives window events from other apps to detect when you leave Instagram or YouTube; for those apps it reads only the package identifier and does not inspect their screens. Detection happens on this device. The service does not intentionally read screen text, messages, passwords, keystrokes, or accessibility descriptions, and it does not send accessibility data to an UNSCROLL server. Detection is experimental and may not work with every app version."
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = hasAccessibilityConsent,
                            onCheckedChange = { hasAccessibilityConsent = it }
                        )
                        Text("I understand and agree to enable this feature.")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = hasAccessibilityConsent,
                    onClick = {
                        consentPrefs.edit()
                            .putBoolean("accessibility_disclosure_v2_accepted", true)
                            .apply()
                        showAccessibilityDisclosure = false
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(onClick = { showAccessibilityDisclosure = false; hasAccessibilityConsent = consentPrefs.getBoolean("accessibility_disclosure_v2_accepted", false) }) {
                    Text("Not now")
                }
            }
        )
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

                // Detection is limited to selected short-form feed identifiers in this version.
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Short-form feed detection only",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp)
                    )
                    Text(
                        text = "This experimental version checks selected Reels/Shorts view IDs only. This behavior cannot currently be changed.",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 11.sp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = AmberWarning.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Daily time quota is not enforced in this prototype. The limit control will return only when it actually counts usage and locks access reliably.",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = AmberWarning, fontSize = 11.sp)
                    )
                }
            }
        }
    }
}
