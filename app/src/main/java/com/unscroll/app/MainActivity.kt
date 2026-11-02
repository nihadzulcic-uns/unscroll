package com.unscroll.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.unscroll.app.data.repository.SettingsRepository
import com.unscroll.app.data.repository.StatsRepository
import com.unscroll.app.ui.screens.*
import com.unscroll.app.ui.theme.*
import com.unscroll.app.ui.viewmodel.UnscrollViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Protection : Screen("protection", "Shield", Icons.Default.Shield)
    object Focus : Screen("focus", "Focus", Icons.Default.SelfImprovement)
    object Sleep : Screen("sleep", "Sleep", Icons.Default.Bedtime)
    object Stats : Screen("stats", "Insights", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: UnscrollViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsRepo = SettingsRepository(applicationContext)
        val statsRepo = StatsRepository(applicationContext)

        setContent {
            viewModel = viewModel {
                UnscrollViewModel(settingsRepo, statsRepo)
            }
            val uiState by viewModel.uiState.collectAsState()

            UNSCROLLTheme {
                UnscrollAppRoot(
                    viewModel = viewModel,
                    uiState = uiState
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.refreshStats()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnscrollTopAppBar(
    currentRoute: String,
    onNavigateToSettings: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Surface(
        color = DarkBackground,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Back button (if on settings) or UNSCROLL logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                if (currentRoute == Screen.Settings.route) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SoftGreenPrimary)
                    )
                }

                Text(
                    text = "UNSCROLL",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = SoftGreenPrimary,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (currentRoute != Screen.Home.route && currentRoute != Screen.Settings.route) {
                    Text(
                        text = "· ${currentRoute.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: Settings Icon (Always visible, securely within the screen edge, min 48dp touch target)
            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (currentRoute == Screen.Settings.route) SoftGreenPrimary else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun UnscrollAppRoot(
    viewModel: UnscrollViewModel,
    uiState: com.unscroll.app.ui.viewmodel.UnscrollUiState
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    // 5 primary bottom navigation items (Material 3 smartphone specification)
    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Protection,
        Screen.Focus,
        Screen.Sleep,
        Screen.Stats
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = DarkBackground,
            topBar = {
                UnscrollTopAppBar(
                    currentRoute = currentRoute,
                    onNavigateToSettings = {
                        if (currentRoute != Screen.Settings.route) {
                            navController.navigate(Screen.Settings.route)
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CharcoalSurface,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                selectedTextColor = SoftGreenPrimary,
                                indicatorColor = SoftGreenPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        uiState = uiState,
                        onNavigateToProtection = { navController.navigate(Screen.Protection.route) },
                        onNavigateToFocus = { navController.navigate(Screen.Focus.route) },
                        onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                        onStartFocusSession = { duration ->
                            viewModel.startFocusSession(duration)
                            navController.navigate(Screen.Focus.route)
                        }
                    )
                }
                composable(Screen.Protection.route) {
                    ProtectionScreen(
                        uiState = uiState,
                        onSetProtectionMode = { viewModel.setProtectionMode(it) },
                        onUpdateInstagram = { viewModel.updateInstagramConfig(it) },
                        onUpdateYouTube = { viewModel.updateYouTubeConfig(it) },
                        onUpdateTikTok = { viewModel.updateTikTokConfig(it) },
                        onSimulateInterception = { appName -> viewModel.simulateInterception(appName) }
                    )
                }
                composable(Screen.Focus.route) {
                    FocusModeScreen(
                        uiState = uiState,
                        onStartFocus = { viewModel.startFocusSession(it) },
                        onPauseFocus = { viewModel.pauseFocusSession() },
                        onCancelFocus = { viewModel.cancelFocusSession() }
                    )
                }
                composable(Screen.Sleep.route) {
                    SleepModeScreen(
                        uiState = uiState,
                        onUpdateSleepConfig = { viewModel.updateSleepSchedule(it) }
                    )
                }
                composable(Screen.Stats.route) {
                    StatisticsScreen(
                        uiState = uiState,
                        onRefreshStats = { viewModel.refreshStats() }
                    )
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        uiState = uiState,
                        onSetLanguage = { viewModel.setLanguage(it) }
                    )
                }
            }
        }

        // Mindful Interception Overlay Shield
        AnimatedVisibility(
            visible = uiState.isInterceptionOverlayActive,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = CharcoalSurfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftGreenPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(SoftGreenPrimary.copy(alpha = 0.15f), RoundedCornerShape(28.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelfImprovement,
                                contentDescription = "Breathe",
                                tint = SoftGreenPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Take a breath.",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftGreenPrimary,
                                fontSize = 20.sp
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "You just opened ${uiState.interceptedAppTitle}. Ask yourself: Is this an intentional choice, or an automatic reflex?",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontSize = 13.sp),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.dismissInterception() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SoftGreenPrimary)
                        ) {
                            Text(
                                text = "Put Down Phone (+1 Thwarted)",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        TextButton(
                            onClick = { viewModel.dismissInterception() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = "Continue for 60s Mindfully",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
