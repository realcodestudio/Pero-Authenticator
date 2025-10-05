package com.rcbs.wearotp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rcbs.wearotp.data.OtpDatabase
import com.rcbs.wearotp.repository.OtpRepository
import com.rcbs.wearotp.ui.screens.AddAccountScreen
import com.rcbs.wearotp.ui.screens.BackupScreen
import com.rcbs.wearotp.ui.screens.MainScreen
import com.rcbs.wearotp.ui.screens.SettingsScreen
import com.rcbs.wearotp.ui.theme.WearOTPTheme
import com.rcbs.wearotp.utils.SettingsManager
import com.rcbs.wearotp.viewmodel.OtpViewModel
import com.rcbs.wearotp.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化数据库和Repository
        val database = OtpDatabase.getDatabase(this)
        val repository = OtpRepository(database.otpDao())
        
        setContent {
            val settingsManager = SettingsManager(this)
            val settings by settingsManager.settings.collectAsState()
            
            WearOTPTheme(
                themeMode = settings.themeMode,
                colorTheme = settings.colorTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OtpApp(repository = repository)
                }
            }
        }
    }
}

@Composable
fun OtpApp(repository: OtpRepository) {
    val navController = rememberNavController()
    val viewModel: OtpViewModel = viewModel { OtpViewModel(repository) }
    
    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onAddManually = {
                    navController.navigate("add_account")
                },
                onNavigateToBackup = {
                    navController.navigate("settings")
                }
            )
        }
        
        composable("add_account") {
            AddAccountScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
        
        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToBackup = {
                    navController.navigate("backup")
                }
            )
        }
        
        composable("backup") {
            BackupScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}