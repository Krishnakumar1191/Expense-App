package com.example.expense.domain.model

data class Income(
    val id: String,
    val source: String,
    val amount: Double,
    val accountId: String,
    val timestamp: Long
)
