package com.example.expense.domain.model

data class Settlement(
    val id: String,
    val groupId: String?,
    val payerId: String,
    val receiverId: String,
    val amount: Double,
    val timestamp: Long
)
