package com.rithika.lock.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rithika.lock.data.model.InterviewCategory
import com.rithika.lock.ui.screens.DashboardScreen
import com.rithika.lock.ui.screens.FeedbackScreen
import com.rithika.lock.ui.screens.HistoryScreen
import com.rithika.lock.ui.screens.InterviewRoomScreen
import com.rithika.lock.ui.screens.LoginScreen
import com.rithika.lock.ui.screens.SettingsScreen
import com.rithika.lock.ui.screens.TopicSelectionScreen
import com.rithika.lock.ui.screens.WelcomeScreen
import com.rithika.lock.ui.viewmodel.InterviewViewModel
import com.rithika.lock.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    object Welcome : Screen("welcome", "Welcome")
    object Login : Screen("login", "Login")
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Dashboard)
    object Topics : Screen("topics", "Topics", Icons.Default.Category)
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object InterviewRoom : Screen("interview_room", "Interview")
    object Feedback : Screen("feedback", "Feedback Report")
}

@Composable
fun MainAppNavigation(
    mainViewModel: MainViewModel,
    interviewViewModel: InterviewViewModel,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.Topics,
        Screen.History,
        Screen.Settings
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Topics.route,
        Screen.History.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = {
                                if (item.icon != null) {
                                    Icon(imageVector = item.icon, contentDescription = item.title)
                                }
                            },
                            label = { Text(item.title) },
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Welcome.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Welcome.route) {
                WelcomeScreen(
                    onContinue = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = mainViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = mainViewModel,
                    onStartTechnical = {
                        navController.navigate("${Screen.Topics.route}?cat=TECHNICAL")
                    },
                    onStartNonTechnical = {
                        navController.navigate("${Screen.Topics.route}?cat=NON_TECHNICAL")
                    },
                    onViewHistory = {
                        navController.navigate(Screen.History.route)
                    }
                )
            }

            composable("${Screen.Topics.route}?cat={cat}") { backStackEntry ->
                val catArg = backStackEntry.arguments?.getString("cat")
                val categoryFilter = when (catArg) {
                    "TECHNICAL" -> InterviewCategory.TECHNICAL
                    "NON_TECHNICAL" -> InterviewCategory.NON_TECHNICAL
                    else -> null
                }

                TopicSelectionScreen(
                    initialCategoryFilter = categoryFilter,
                    onTopicSelected = { topicId, difficulty ->
                        interviewViewModel.startNewSession(topicId, difficulty)
                        navController.navigate(Screen.InterviewRoom.route)
                    }
                )
            }

            composable(Screen.InterviewRoom.route) {
                InterviewRoomScreen(
                    viewModel = interviewViewModel,
                    onInterviewFinished = {
                        mainViewModel.refreshSessions()
                        navController.navigate(Screen.Feedback.route) {
                            popUpTo(Screen.InterviewRoom.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Feedback.route) {
                FeedbackScreen(
                    viewModel = interviewViewModel,
                    onReturnToDashboard = {
                        mainViewModel.refreshSessions()
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(viewModel = mainViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = mainViewModel)
            }
        }
    }
}
