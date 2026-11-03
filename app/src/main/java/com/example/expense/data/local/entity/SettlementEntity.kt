package com.example.expense.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settlements")
data class SettlementEntity(
    @PrimaryKey
    val id: String,
    val groupId: String?,
    val payerId: String,
    val receiverId: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
