package com.example.expense.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "expense_splits",
    primaryKeys = ["expenseId", "userId"]
)
data class ExpenseSplitEntity(
    val expenseId: String,
    val userId: String,
    val amountOwed: Double
)
