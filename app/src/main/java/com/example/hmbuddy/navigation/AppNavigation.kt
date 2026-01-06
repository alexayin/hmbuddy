package com.example.hmbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.hmbuddy.ui.screens.HomeScreen
import com.example.hmbuddy.ui.screens.LogRunScreen
import com.example.hmbuddy.ui.screens.ProfileScreen
import com.example.hmbuddy.ui.screens.RunHistoryScreen
import com.example.hmbuddy.ui.screens.WeeklyTargetsScreen
import com.example.hmbuddy.viewmodel.ProfileViewModel
import com.example.hmbuddy.viewmodel.RunViewModel
import com.example.hmbuddy.viewmodel.TargetViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object LogRun : Screen("log_run")
    object RunHistory : Screen("run_history")
    object WeeklyTargets : Screen("weekly_targets")
    object Profile : Screen("profile")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    runViewModel: RunViewModel,
    targetViewModel: TargetViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                runViewModel = runViewModel,
                targetViewModel = targetViewModel,
                profileViewModel = profileViewModel,
                onLogRunClick = { navController.navigate(Screen.LogRun.route) },
                onRunHistoryClick = { navController.navigate(Screen.RunHistory.route) },
                onWeeklyTargetsClick = { navController.navigate(Screen.WeeklyTargets.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(Screen.LogRun.route) {
            LogRunScreen(
                runViewModel = runViewModel,
                onRunLogged = { navController.popBackStack() }
            )
        }

        composable(Screen.RunHistory.route) {
            RunHistoryScreen(runViewModel = runViewModel)
        }

        composable(Screen.WeeklyTargets.route) {
            WeeklyTargetsScreen(
                targetViewModel = targetViewModel,
                onTargetsSaved = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                profileViewModel = profileViewModel,
                onProfileSaved = { navController.popBackStack() }
            )
        }
    }
}
