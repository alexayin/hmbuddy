package com.example.hmbuddy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hmbuddy.navigation.AppNavigation
import com.example.hmbuddy.navigation.Screen
import com.example.hmbuddy.ui.theme.HmBuddyTheme
import com.example.hmbuddy.viewmodel.ProfileViewModel
import com.example.hmbuddy.viewmodel.RunViewModel
import com.example.hmbuddy.viewmodel.TargetViewModel
import com.example.hmbuddy.viewmodel.WeeklyAchievementViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as HmBuddyApplication

        enableEdgeToEdge()
        setContent {
            HmBuddyTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val runViewModel: RunViewModel = viewModel(
                    factory = RunViewModel.Factory(app.runLogRepository)
                )
                val targetViewModel: TargetViewModel = viewModel(
                    factory = TargetViewModel.Factory(app.weeklyTargetRepository, app.raceGoalRepository)
                )
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(app.userProfileRepository)
                )
                val achievementViewModel: WeeklyAchievementViewModel = viewModel(
                    factory = WeeklyAchievementViewModel.Factory(app.weeklyAchievementRepository)
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (currentRoute != Screen.Home.route) {
                            TopAppBar(
                                title = {
                                    Text(
                                        when (currentRoute) {
                                            Screen.LogRun.route -> "Log Run"
                                            Screen.RunHistory.route -> "Run History"
                                            Screen.WeeklyTargets.route -> "Targets"
                                            Screen.Profile.route -> "Profile"
                                            else -> ""
                                        }
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        runViewModel = runViewModel,
                        targetViewModel = targetViewModel,
                        profileViewModel = profileViewModel,
                        achievementViewModel = achievementViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
