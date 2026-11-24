package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FileAndStorageScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MediaControlScreen
import com.example.ui.screens.PermissionsScreen
import com.example.ui.screens.SchedulerScreen
import com.example.ui.screens.ScreenAutomationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SmsAutomationScreen
import com.example.ui.screens.SocialAutomationScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TelegramAutomationScreen
import com.example.ui.screens.VoiceAssistantScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.screens.WhatsAppAutomationScreen

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val DASHBOARD = "dashboard"
    const val VOICE_ASSISTANT = "voice_assistant"
    const val SCREEN_CONTROL = "screen_control"
    const val MEDIA_CONTROL = "media_control"
    const val FILES_STORAGE = "files_storage"
    const val WHATSAPP = "whatsapp"
    const val SMS = "sms"
    const val TELEGRAM = "telegram"
    const val SOCIAL = "social"
    const val SCHEDULER = "scheduler"
    const val HISTORY = "history"
    const val PERMISSIONS = "permissions"
    const val SETTINGS = "settings"
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    navController: NavHostController = rememberNavController()
) {
    LaunchedEffect(Unit) {
        viewModel.navigationEvents.collect { target ->
            when (target) {
                "media" -> navController.navigate(Routes.MEDIA_CONTROL)
                "files" -> navController.navigate(Routes.FILES_STORAGE)
                "screen" -> navController.navigate(Routes.SCREEN_CONTROL)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onSelectMode = { _ ->
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToVoice = { navController.navigate(Routes.VOICE_ASSISTANT) },
                onNavigateToScreenControl = { navController.navigate(Routes.SCREEN_CONTROL) },
                onNavigateToMedia = { navController.navigate(Routes.MEDIA_CONTROL) },
                onNavigateToFiles = { navController.navigate(Routes.FILES_STORAGE) },
                onNavigateToWhatsApp = { navController.navigate(Routes.WHATSAPP) },
                onNavigateToSms = { navController.navigate(Routes.SMS) },
                onNavigateToTelegram = { navController.navigate(Routes.TELEGRAM) },
                onNavigateToSocial = { navController.navigate(Routes.SOCIAL) },
                onNavigateToScheduler = { navController.navigate(Routes.SCHEDULER) },
                onNavigateToHistory = { navController.navigate(Routes.HISTORY) },
                onNavigateToPermissions = { navController.navigate(Routes.PERMISSIONS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.VOICE_ASSISTANT) {
            VoiceAssistantScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.SCREEN_CONTROL) {
            ScreenAutomationScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.MEDIA_CONTROL) {
            MediaControlScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.FILES_STORAGE) {
            FileAndStorageScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.WHATSAPP) {
            WhatsAppAutomationScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.SMS) {
            SmsAutomationScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.TELEGRAM) {
            TelegramAutomationScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.SOCIAL) {
            SocialAutomationScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.SCHEDULER) {
            SchedulerScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.HISTORY) {
            HistoryScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
