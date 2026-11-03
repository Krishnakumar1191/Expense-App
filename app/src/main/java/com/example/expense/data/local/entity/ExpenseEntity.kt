package com.example.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val groupId: String?,
    val description: String,
    val totalAmount: Double,
    val paidByUserId: String,
    val category: String = "Other",
    val isPersonal: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
