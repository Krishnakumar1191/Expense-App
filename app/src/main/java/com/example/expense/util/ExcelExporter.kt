package com.example.expense.util

import com.example.expense.domain.model.Expense
import com.example.expense.domain.model.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    fun generateCsvReport(
        monthName: String,
        expenses: List<Expense>,
        users: List<User>
    ): String {
        val userMap = users.associateBy { it.id }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val sb = StringBuilder()
        sb.append("Date,Type,Category,Description,Amount (₹),Paid By\n")

        for (expense in expenses) {
            val dateStr = dateFormat.format(Date(expense.timestamp))
            val typeStr = if (expense.isPersonal) "Personal" else "Group"
            val categoryStr = escapeCsv(expense.category)
            val descStr = escapeCsv(expense.description)
            val amountStr = String.format(Locale.US, "%.2f", expense.totalAmount)
            val payerName = escapeCsv(userMap[expense.paidByUserId]?.name ?: expense.paidByUserId)

            sb.append("$dateStr,$typeStr,$categoryStr,$descStr,$amountStr,$payerName\n")
        }

        val totalAmount = expenses.sumOf { it.totalAmount }
        sb.append("\nTotal Expenses,$monthName,,,${String.format(Locale.US, "%.2f", totalAmount)},\n")

        return sb.toString()
    }

    private fun escapeCsv(text: String): String {
        return if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    }
}
