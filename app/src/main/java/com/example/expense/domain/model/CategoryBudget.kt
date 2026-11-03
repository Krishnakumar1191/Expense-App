package com.example.expense.domain.model

data class CategoryBudget(
    val categoryName: String,
    val monthlyLimit: Double,
    val currentSpent: Double = 0.0
)
