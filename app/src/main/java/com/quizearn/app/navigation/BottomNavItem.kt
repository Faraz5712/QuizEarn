package com.quizearn.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.Tasks, Icons.Default.Assignment, "Tasks"),
    BottomNavItem(Screen.Leaderboard, Icons.Default.EmojiEvents, "Ranks"),
    BottomNavItem(Screen.Wallet, Icons.Default.AccountBalanceWallet, "Wallet"),
    BottomNavItem(Screen.Profile, Icons.Default.Person, "Profile")
)
