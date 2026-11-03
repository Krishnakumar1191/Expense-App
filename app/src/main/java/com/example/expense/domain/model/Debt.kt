package com.example.expense.domain.model

data class Debt(
    val fromUser: User,
    val toUser: User,
    val amount: Double
)
