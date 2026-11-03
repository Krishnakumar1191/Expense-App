package com.example.expense.domain.model

data class ExpenseSplit(
    val expenseId: String,
    val userId: String,
    val amountOwed: Double
)
