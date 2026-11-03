package com.example.expense.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expense.ui.dashboard.DashboardScreen
import com.example.expense.ui.expense.AddExpenseScreen
import com.example.expense.ui.friends.FriendsScreen
import com.example.expense.ui.groups.GroupDetailScreen
import com.example.expense.ui.groups.GroupsScreen
import com.example.expense.ui.settlement.SettleUpScreen
import com.example.expense.ui.upi.UpiTimelineScreen
import com.example.expense.ui.wallet.WalletScreen

@Composable
fun ExpenseApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.Wallet,
        Screen.UpiSync,
        Screen.Groups,
        Screen.Friends
    )

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Wallet.route,
        Screen.UpiSync.route,
        Screen.Groups.route,
        Screen.Friends.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                when (screen) {
                                    Screen.Dashboard -> Icon(Icons.Default.Dashboard, contentDescription = "Dashboard")
                                    Screen.Wallet -> Icon(Icons.Default.AccountBalance, contentDescription = "Wallet")
                                    Screen.UpiSync -> Icon(Icons.Default.QrCodeScanner, contentDescription = "UPI Sync")
                                    Screen.Groups -> Icon(Icons.Default.Group, contentDescription = "Groups")
                                    Screen.Friends -> Icon(Icons.Default.Person, contentDescription = "Friends")
                                    else -> {}
                                }
                            },
                            label = {
                                when (screen) {
                                    Screen.Dashboard -> Text("Dashboard")
                                    Screen.Wallet -> Text("Wallet")
                                    Screen.UpiSync -> Text("UPI Sync")
                                    Screen.Groups -> Text("Groups")
                                    Screen.Friends -> Text("Friends")
                                    else -> {}
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToAddExpense = { navController.navigate(Screen.AddExpense.route) },
                    onNavigateToSettleUp = { navController.navigate(Screen.SettleUp.route) }
                )
            }

            composable(Screen.Wallet.route) {
                WalletScreen()
            }

            composable(Screen.UpiSync.route) {
                UpiTimelineScreen()
            }

            composable(Screen.Groups.route) {
                GroupsScreen(
                    onGroupClick = { groupId ->
                        navController.navigate(Screen.GroupDetail.createRoute(groupId))
                    }
                )
            }

            composable(Screen.GroupDetail.route) {
                GroupDetailScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Friends.route) {
                FriendsScreen()
            }

            composable(Screen.AddExpense.route) {
                AddExpenseScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.SettleUp.route) {
                SettleUpScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
