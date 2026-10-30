package com.quizearn.app.navigation

sealed class Screen(val route: String, val title: String) {
    data object Splash : Screen("splash", "Splash")
    data object Onboarding : Screen("onboarding", "Welcome")
    data object Auth : Screen("auth", "Authentication")
    data object SignIn : Screen("sign_in", "Sign In")
    data object SignUp : Screen("sign_up", "Create Account")
    data object ForgotPassword : Screen("forgot_password", "Forgot Password")
    data object Home : Screen("home", "Home")
    data object CategoryList : Screen("category_list", "Categories")
    data object Tasks : Screen("tasks", "Daily Tasks")
    data object TournamentLobby : Screen("tournament_lobby", "Tournaments")
    data object Leaderboard : Screen("leaderboard", "Leaderboard")
    data object Wallet : Screen("wallet", "My Wallet")
    data object Profile : Screen("profile", "Profile")
    data object QuizPlay : Screen("quiz_play/{categoryId}", "Quiz") {
        fun createRoute(categoryId: String) = "quiz_play/$categoryId"
    }
    data object QuizResult : Screen("quiz_result/{sessionId}?tournamentId={tournamentId}", "Results") {
        fun createRoute(sessionId: String, tournamentId: String? = null) =
            if (tournamentId.isNullOrBlank()) {
                "quiz_result/$sessionId"
            } else {
                "quiz_result/$sessionId?tournamentId=$tournamentId"
            }
    }
    data object QuizHistory : Screen("quiz_history", "Quiz History")
    data object Settings : Screen("settings", "Settings")
    data object Help : Screen("help", "Help & Support")
    data object TransactionHistory : Screen("transaction_history", "Transaction History")
    data object RewardCatalog : Screen("reward_catalog", "Reward Catalog")
    data object Notifications : Screen("notifications", "Notifications")
}
