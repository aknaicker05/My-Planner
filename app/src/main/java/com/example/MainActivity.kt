package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.PlannerViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PlannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val currentUser by viewModel.currentUser.collectAsState()

                // Redirect to Auth or Main based on Session status change
                LaunchedEffect(currentUser) {
                    if (currentUser == null) {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = if (currentUser == null) "auth" else "main",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("auth") {
                        AuthScreen(
                            viewModel = viewModel,
                            onAuthSuccess = {
                                navController.navigate("main") {
                                    popUpTo("auth") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("main") {
                        MainAppLayout(
                            viewModel = viewModel,
                            onNavigateToEvent = { eventId ->
                                navController.navigate("event_detail/$eventId")
                            },
                            onNavigateToAddEvent = {
                                navController.navigate("add_edit_event?eventId=0")
                            },
                            onNavigateToEditEvent = { eventId ->
                                navController.navigate("add_edit_event?eventId=$eventId")
                            },
                            onLogout = {
                                navController.navigate("auth") {
                                    popUpTo("main") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = "event_detail/{eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
                        EventDetailScreen(
                            viewModel = viewModel,
                            eventId = eventId,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEdit = { editId ->
                                navController.navigate("add_edit_event?eventId=$editId")
                            }
                        )
                    }

                    composable(
                        route = "add_edit_event?eventId={eventId}",
                        arguments = listOf(navArgument("eventId") { type = NavType.IntType; defaultValue = 0 })
                    ) { backStackEntry ->
                        val eventId = backStackEntry.arguments?.getInt("eventId") ?: 0
                        AddEditEventScreen(
                            viewModel = viewModel,
                            eventId = if (eventId == 0) null else eventId,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppLayout(
    viewModel: PlannerViewModel,
    onNavigateToEvent: (Int) -> Unit,
    onNavigateToAddEvent: () -> Unit,
    onNavigateToEditEvent: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp > 600

    Scaffold(
        bottomBar = {
            if (!isWideScreen) {
                NavigationBar(modifier = Modifier.testTag("mobile_bottom_bar")) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(if (selectedTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard, "Dashboard") },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("nav_tab_0")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(if (selectedTab == 1) Icons.Default.EventNote else Icons.Outlined.EventNote, "My Events") },
                        label = { Text("My Events") },
                        modifier = Modifier.testTag("nav_tab_1")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(if (selectedTab == 2) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth, "Calendar") },
                        label = { Text("Calendar") },
                        modifier = Modifier.testTag("nav_tab_2")
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(if (selectedTab == 3) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle, "Profile") },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("nav_tab_3")
                    )
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isWideScreen) {
                NavigationRail(
                    modifier = Modifier.testTag("tablet_nav_rail"),
                    header = {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Global Logo Globe",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .size(36.dp)
                        )
                    }
                ) {
                    NavigationRailItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(if (selectedTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard, "Dashboard") },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("nav_rail_tab_0")
                    )
                    NavigationRailItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(if (selectedTab == 1) Icons.Default.EventNote else Icons.Outlined.EventNote, "My Events") },
                        label = { Text("My Events") },
                        modifier = Modifier.testTag("nav_rail_tab_1")
                    )
                    NavigationRailItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(if (selectedTab == 2) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth, "Calendar") },
                        label = { Text("Calendar") },
                        modifier = Modifier.testTag("nav_rail_tab_2")
                    )
                    NavigationRailItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = { Icon(if (selectedTab == 3) Icons.Default.AccountCircle else Icons.Outlined.AccountCircle, "Profile") },
                        label = { Text("Profile") },
                        modifier = Modifier.testTag("nav_rail_tab_3")
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                when (selectedTab) {
                    0 -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToEvent = onNavigateToEvent,
                        onNavigateToMyEvents = { selectedTab = 1 }
                    )
                    1 -> MyEventsScreen(
                        viewModel = viewModel,
                        onNavigateToAddEvent = onNavigateToAddEvent,
                        onNavigateToEditEvent = onNavigateToEditEvent,
                        onNavigateToDetail = onNavigateToEvent
                    )
                    2 -> CalendarScreen(
                        viewModel = viewModel,
                        onNavigateToEvent = onNavigateToEvent
                    )
                    3 -> ProfileScreen(
                        viewModel = viewModel,
                        onLogoutSuccess = onLogout
                    )
                }
            }
        }
    }
}
