package com.example.expense.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Wallet : Screen("wallet")
    object Groups : Screen("groups")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
    object Friends : Screen("friends")
    object AddExpense : Screen("add_expense")
    object SettleUp : Screen("settle_up")
    object UpiSync : Screen("upi_sync")
}
