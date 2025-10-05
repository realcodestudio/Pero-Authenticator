package com.rcbs.authenticator.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rcbs.authenticator.ui.screens.*

@Composable
fun MainNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                onAddManually = { navController.navigate("add") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToBackup = { navController.navigate("backup") },
                onNavigateToSync = { navController.navigate("sync") }
            )
        }
        
        composable("add") {
            AddAccountScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBackup = { navController.navigate("backup") }
            )
        }
        
        composable("backup") {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("sync") {
            SyncScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}