package com.unscroll.app.ui.screens

import android.content.Intent
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unscroll.app.data.model.ProtectionMode
import com.unscroll.app.data.model.ServiceStatus
import com.unscroll.app.ui.theme.*
import com.unscroll.app.ui.viewmodel.UnscrollUiState

@Composable
fun HomeScreen(
    uiState: UnscrollUiState,
    onNavigateToProtection: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToStats: () -> Unit,
    onStartFocusSession: (Int) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Daily Greeting & Mode Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "Mindful Presence",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Daily digital wellbeing summary",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondary,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Mode indicator
            Surface(
                color = CharcoalSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                if (uiState.protectionMode == ProtectionMode.AUTOMATIC) SoftGreenPrimary else AmberWarning
                            )
                    )
                    Text(
                        text = uiState.protectionMode.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Screen Time Hero Card (Honest Data Only)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToStats() },
            shape = RoundedCornerShape(18.dp),
            color = CharcoalSurface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (uiState.overview.isRealUsageStatsAvailable) CharcoalSurfaceBorder else AmberWarning.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Social Screen Time",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                    if (uiState.overview.isRealUsageStatsAvailable) {
                        Text(
                            text = "Verified Telemetry",
                            style = MaterialTheme.typography.labelSmall.copy(color = SoftGreenPrimary)
                        )
                    } else {
                        Text(
                            text = "Permission Required",
                            style = MaterialTheme.typography.labelSmall.copy(color = AmberWarning)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.overview.isRealUsageStatsAvailable) {
                    val hours = uiState.overview.totalScreenTimeMinutes / 60
                    val mins = uiState.overview.totalScreenTimeMinutes % 60

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${hours}h ${mins}m",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 38.sp,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "today",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feed entries are detections, not guaranteed successful overlays or time saved.
                    Column {
                        Text(
                            text = "Feed entries detected",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                        Text(
                            text = "${uiState.overview.totalInterceptions} entries detected",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                } else {
                    // Empty state / Permission prompt
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "--",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 36.sp,
                                color = TextMuted
                            )
                        )
                        Text(
                            text = "Screen time measurement requires Android Usage Access permission. No fabricated statistics are shown.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(text = "Grant Usage Access", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Focus Mode Section
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = CharcoalSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Focus",
                            tint = SoftGreenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Focus Mode",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        )
                    }

                    if (uiState.focusSession.isActive) {
                        Text(
                            text = "ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SoftGreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.focusSession.isActive) {
                    val remainingMins = uiState.focusSession.remainingSeconds / 60
                    val remainingSecs = uiState.focusSession.remainingSeconds % 60
                    Text(
                        text = String.format("%02d:%02d remaining", remainingMins, remainingSecs),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = SoftGreenPrimary,
                            fontSize = 22.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onNavigateToFocus,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftGreenPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(text = "Manage Active Session", color = DarkBackground, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Text(
                        text = "Use a simple focus timer to stay mindful. This timer does not block other apps.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 25, 45).forEach { mins ->
                            OutlinedButton(
                                onClick = { onStartFocusSession(mins) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CharcoalSurface,
                                    contentColor = TextPrimary
                                )
                            ) {
                                Text(text = "${mins}m", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Protected Applications Quick View
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Protected Apps",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )
            Text(
                text = "Configure",
                style = MaterialTheme.typography.bodySmall.copy(color = SoftGreenPrimary),
                modifier = Modifier.clickable { onNavigateToProtection() }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // App cards with honest ServiceStatus
        AppStatusRow(
            name = "Instagram",
            rule = "Experimental Reels detection",
            status = uiState.instagramConfig.status,
            onClick = onNavigateToProtection
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppStatusRow(
            name = "YouTube",
            rule = "Experimental Shorts detection",
            status = uiState.youtubeConfig.status,
            onClick = onNavigateToProtection
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppStatusRow(
            name = "TikTok",
            rule = "Feed Delay & Session Limit",
            status = uiState.tiktokConfig.status,
            onClick = onNavigateToProtection
        )
    }
}

@Composable
private fun AppStatusRow(
    name: String,
    rule: String,
    status: ServiceStatus,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = CharcoalSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = rule,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                )
            }

            // Verified Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (status) {
                                ServiceStatus.ACTIVE -> SoftGreenPrimary
                                ServiceStatus.PERMISSION_REQUIRED -> AmberWarning
                                ServiceStatus.NOT_CONFIGURED -> TextMuted
                                ServiceStatus.ERROR -> RoseDestructive
                            }
                        )
                )
                Text(
                    text = status.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        color = when (status) {
                            ServiceStatus.ACTIVE -> SoftGreenPrimary
                            ServiceStatus.PERMISSION_REQUIRED -> AmberWarning
                            ServiceStatus.NOT_CONFIGURED -> TextMuted
                            ServiceStatus.ERROR -> RoseDestructive
                        }
                    )
                )
            }
        }
    }
}
