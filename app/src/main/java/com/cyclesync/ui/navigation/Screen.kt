package com.cyclesync.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Main : Screen("main")
    data object Settings : Screen("settings")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object Tracking : Screen("tracking/{date}") {
        fun createRoute(date: String) = "tracking/$date"
    }
}

sealed class MainTab(val route: String, val label: String) {
    data object CycleView : MainTab("cycle_view", "Cycle")
    data object Calendar : MainTab("calendar", "Calendar")
    data object Analysis : MainTab("analysis", "Analysis")
    data object Content : MainTab("content", "Learn")
}
