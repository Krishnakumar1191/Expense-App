package com.example.expense.domain.model

data class UserBalance(
    val user: User,
    val netBalance: Double // Positive = User is owed money, Negative = User owes money
)
