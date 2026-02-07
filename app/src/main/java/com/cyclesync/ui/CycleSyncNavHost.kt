package com.cyclesync.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cyclesync.ui.demo.DemoScreen
import com.cyclesync.ui.login.LoginScreen
import com.cyclesync.ui.login.LoginViewModel
import com.cyclesync.ui.navigation.Screen
import com.cyclesync.ui.onboarding.OnboardingScreen
import com.cyclesync.ui.onboarding.OnboardingViewModel
import com.cyclesync.ui.settings.SettingsScreen

@Composable
fun CycleSyncNavHost(
    navController: NavHostController = rememberNavController()
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val isOnboardingCompleted by onboardingViewModel.isOnboardingCompleted.collectAsState()

    val loginViewModel: LoginViewModel = hiltViewModel()
    val hasPinSet by loginViewModel.hasPinSet.collectAsState()

    val startDestination = when {
        !isOnboardingCompleted -> Screen.Onboarding.route
        hasPinSet -> Screen.Login.route
        else -> Screen.Demo.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            val isSetup = !hasPinSet
            LoginScreen(
                isSetup = isSetup,
                onAuthenticated = {
                    navController.navigate(Screen.Demo.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Demo.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Demo.route) {
            DemoScreen(
                onContinue = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Demo.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToTracking = { date ->
                    navController.navigate(Screen.Tracking.createRoute(date))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Tracking.route) { backStackEntry ->
            val date = backStackEntry.arguments?.getString("date") ?: return@composable
            com.cyclesync.ui.tracking.TrackingScreen(
                date = date,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
