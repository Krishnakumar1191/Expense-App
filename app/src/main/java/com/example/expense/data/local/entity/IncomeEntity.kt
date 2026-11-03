package com.example.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey
    val id: String,
    val source: String,
    val amount: Double,
    val accountId: String,
    val timestamp: Long = System.currentTimeMillis()
)
