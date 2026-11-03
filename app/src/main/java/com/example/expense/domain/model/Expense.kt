package com.example.expense.domain.model

data class Expense(
    val id: String,
    val groupId: String?,
    val description: String,
    val totalAmount: Double,
    val paidByUserId: String,
    val category: String = "Other",
    val isPersonal: Boolean = false,
    val timestamp: Long,
    val splits: List<ExpenseSplit> = emptyList()
)
