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
fun StatisticsScreen(
    uiState: UnscrollUiState,
    onRefreshStats: () -> Unit
) {
    var selectedTimeframe by remember { mutableStateOf("Daily") }
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
            text = "Usage Insights",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
        )
        Text(
            text = "Verified metrics on screen time and thwarted doomscroll loops",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Honest Data Source Disclosure Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = if (uiState.overview.isRealUsageStatsAvailable) CharcoalSurface else CharcoalSurfaceElevated,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (uiState.overview.isRealUsageStatsAvailable) SoftGreenPrimary else AmberWarning
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.overview.isRealUsageStatsAvailable) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = "Data Notice",
                        tint = if (uiState.overview.isRealUsageStatsAvailable) SoftGreenPrimary else AmberWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (uiState.overview.isRealUsageStatsAvailable) "Live Android UsageStats Active" else "Permission Required for Live Telemetry",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.overview.dataNotice,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                )

                if (!uiState.overview.isRealUsageStatsAvailable) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "Grant Usage Access in Settings", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (uiState.overview.isRealUsageStatsAvailable) {
            // Timeframe Segmented Control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CharcoalSurface, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                listOf("Daily", "Weekly").forEach { timeframe ->
                    val isSelected = selectedTimeframe == timeframe
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTimeframe = timeframe },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CharcoalSurfaceElevated else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, SoftGreenPrimary) else null
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = timeframe,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SoftGreenPrimary else TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real Metrics Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "VERIFIED TODAY",
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
                        Column {
                            Text(
                                text = "Total Screen Time",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                            Text(
                                text = "${uiState.overview.totalScreenTimeMinutes / 60}h ${uiState.overview.totalScreenTimeMinutes % 60}m",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Interceptions",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                            Text(
                                text = "${uiState.overview.totalInterceptions}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SoftGreenPrimary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Breakdown by App
            Text(
                text = "APP BREAKDOWN",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    fontSize = 10.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.appUsageList.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CharcoalSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No foreground usage recorded for target apps today.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            } else {
                uiState.appUsageList.forEach { usage ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = CharcoalSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = usage.targetApp.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = usage.targetApp.packageName,
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontSize = 10.sp)
                                )
                            }

                            Text(
                                text = "${usage.usageMinutes}m",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }
                    }
                }
            }
        } else {
            // Empty State view when permission is not granted
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CharcoalSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CharcoalSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Empty Stats",
                        tint = TextMuted,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        text = "No Data Available",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "UNSCROLL strictly does not show fabricated statistics. Once Usage Access is enabled, real daily and weekly application metrics will populate here automatically.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
