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
import com.example.hmbuddy.data.AppDatabase
import com.example.hmbuddy.navigation.AppNavigation
import com.example.hmbuddy.navigation.Screen
import com.example.hmbuddy.ui.theme.AndroidTestTheme
import com.example.hmbuddy.viewmodel.ProfileViewModel
import com.example.hmbuddy.viewmodel.RunViewModel
import com.example.hmbuddy.viewmodel.TargetViewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val runDao = database.runDao()
        val weeklyTargetDao = database.weeklyTargetDao()
        val userProfileDao = database.userProfileDao()

        enableEdgeToEdge()
        setContent {
            AndroidTestTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val runViewModel: RunViewModel = viewModel(
                    factory = RunViewModel.Factory(runDao)
                )
                val targetViewModel: TargetViewModel = viewModel(
                    factory = TargetViewModel.Factory(weeklyTargetDao)
                )
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(userProfileDao)
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
                                            Screen.WeeklyTargets.route -> "Weekly Targets"
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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


