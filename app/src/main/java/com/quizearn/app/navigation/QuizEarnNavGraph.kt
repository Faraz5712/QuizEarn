package com.quizearn.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.quizearn.app.ui.screens.auth.AuthScreen
import com.quizearn.app.ui.screens.auth.ForgotPasswordScreen
import com.quizearn.app.ui.screens.auth.SignInScreen
import com.quizearn.app.ui.screens.auth.SignUpScreen
import com.quizearn.app.ui.screens.category.CategoryListScreen
import com.quizearn.app.ui.screens.help.HelpScreen
import com.quizearn.app.ui.screens.home.HomeScreen
import com.quizearn.app.ui.screens.leaderboard.LeaderboardScreen
import com.quizearn.app.ui.screens.notifications.NotificationInboxScreen
import com.quizearn.app.ui.screens.onboarding.OnboardingScreen
import com.quizearn.app.ui.screens.profile.ProfileScreen
import com.quizearn.app.ui.screens.quiz.QuizHistoryScreen
import com.quizearn.app.ui.screens.quiz.QuizPlayScreen
import com.quizearn.app.ui.screens.quiz.QuizResultScreen
import com.quizearn.app.ui.screens.settings.SettingsScreen
import com.quizearn.app.ui.screens.splash.SplashScreen
import com.quizearn.app.ui.screens.tasks.TasksScreen
import com.quizearn.app.ui.screens.tournament.TournamentLobbyScreen
import com.quizearn.app.ui.screens.wallet.RewardCatalogScreen
import com.quizearn.app.ui.screens.wallet.TransactionHistoryScreen
import com.quizearn.app.ui.screens.wallet.WalletScreen

@Composable
fun QuizEarnNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screen.Home.route,
        Screen.Tasks.route,
        Screen.Leaderboard.route,
        Screen.Wallet.route,
        Screen.Profile.route
    )
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.screen.route) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToOnboarding = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onCompleteOnboarding = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Auth.route) {
                SignInScreen(
                    onSignInSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            composable(Screen.SignIn.route) {
                SignInScreen(
                    onSignInSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignIn.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Screen.SignUp.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    }
                )
            }

            composable(Screen.SignUp.route) {
                SignUpScreen(
                    onSignUpSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.SignUp.route) { inclusive = true }
                        }
                    },
                    onNavigateToSignIn = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onNavigateToSignIn = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    onStartQuiz = { categoryId ->
                        navController.navigate(Screen.QuizPlay.createRoute(categoryId))
                    },
                    onViewWallet = {
                        navController.navigate(Screen.Wallet.route)
                    },
                    onViewNotifications = {
                        navController.navigate(Screen.Notifications.route)
                    },
                    onNavigateToTasks = {
                        navController.navigate(Screen.Tasks.route)
                    }
                )
            }

            composable(Screen.CategoryList.route) {
                CategoryListScreen(
                    onCategoryClick = { categoryId ->
                        navController.navigate(Screen.QuizPlay.createRoute(categoryId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Tasks.route) {
                TasksScreen(
                    onNavigateToTournaments = {
                        navController.navigate(Screen.TournamentLobby.route)
                    }
                )
            }

            composable(Screen.TournamentLobby.route) {
                TournamentLobbyScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onEnterTournament = { tournamentId, categoryId ->
                        navController.navigate(Screen.QuizPlay.createRoute(categoryId))
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "tournamentId",
                            tournamentId
                        )
                    }
                )
            }

            composable(Screen.Leaderboard.route) {
                LeaderboardScreen()
            }

            composable(Screen.Wallet.route) {
                WalletScreen(
                    onNavigateToRewardCatalog = {
                        navController.navigate(Screen.RewardCatalog.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.TransactionHistory.route)
                    },
                    onNavigateToEarnMore = {
                        navController.navigate(Screen.CategoryList.route)
                    }
                )
            }

            composable(Screen.TransactionHistory.route) {
                TransactionHistoryScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.RewardCatalog.route) {
                RewardCatalogScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onSignOut = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onNavigateToQuizHistory = {
                        navController.navigate(Screen.QuizHistory.route)
                    },
                    onNavigateToAchievements = {
                        navController.navigate(Screen.Tasks.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToHelp = {
                        navController.navigate(Screen.Help.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToHelp = {
                        navController.navigate(Screen.Help.route)
                    },
                    onSignOut = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onAccountDeleted = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Help.route) {
                HelpScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Notifications.route) {
                NotificationInboxScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToRoute = { route ->
                        navController.navigate(route)
                    }
                )
            }

            composable(Screen.QuizPlay.route) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: "general"
                QuizPlayScreen(
                    categoryId = categoryId,
                    onSubmitQuiz = { sessionId ->
                        val tournamentId = backStackEntry.savedStateHandle.get<String>("tournamentId")
                        navController.navigate(Screen.QuizResult.createRoute(sessionId, tournamentId)) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onForfeit = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.QuizResult.route,
                arguments = listOf(
                    navArgument("tournamentId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: "session"
                val tournamentId = backStackEntry.arguments?.getString("tournamentId")
                QuizResultScreen(
                    sessionId = sessionId,
                    tournamentId = tournamentId,
                    onPlayAgain = {
                        navController.navigate(Screen.QuizPlay.createRoute("cat_science")) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onBackHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.QuizHistory.route) {
                QuizHistoryScreen(
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
