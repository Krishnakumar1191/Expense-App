package com.example.expense.util

import com.example.expense.domain.model.ExpenseCategory

enum class TransactionType {
    DEBIT, CREDIT
}

data class ParsedUpiTransaction(
    val amount: Double,
    val type: TransactionType,
    val merchantOrSender: String,
    val bankName: String,
    val timestamp: Long,
    val rawSms: String,
    val category: ExpenseCategory
)
