package com.example.expense.ui.upi

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense.domain.model.Expense
import com.example.expense.domain.model.ExpenseSplit
import com.example.expense.domain.repository.ExpenseRepository
import com.example.expense.util.ParsedUpiTransaction
import com.example.expense.util.SmsHistoryScanner
import com.example.expense.util.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class DailyUpiGroup(
    val dateLabel: String,
    val dateTimestamp: Long,
    val dailyExpenseTotal: Double,
    val transactions: List<ParsedUpiTransaction>
)

data class UpiUiState(
    val isScanning: Boolean = false,
    val dailyGroups: List<DailyUpiGroup> = emptyList(),
    val monthlyExpenseTotal: Double = 0.0,
    val savedExpenseIds: Set<String> = emptySet()
)

@HiltViewModel
class UpiViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpiUiState())
    val uiState: StateFlow<UpiUiState> = _uiState

    fun scanSms(context: Context) {
        _uiState.value = _uiState.value.copy(isScanning = true)
        viewModelScope.launch {
            val transactions = withContext(Dispatchers.IO) {
                SmsHistoryScanner.scanInbox(context)
            }

            val grouped = groupTransactionsByDay(transactions)

            val cal = Calendar.getInstance()
            val currentMonth = cal.get(Calendar.MONTH)
            val currentYear = cal.get(Calendar.YEAR)

            val monthlyTotal = transactions.filter {
                it.type == TransactionType.DEBIT && run {
                    cal.timeInMillis = it.timestamp
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                }
            }.sumOf { it.amount }

            _uiState.value = _uiState.value.copy(
                isScanning = false,
                dailyGroups = grouped,
                monthlyExpenseTotal = monthlyTotal
            )
        }
    }

    fun saveAsExpense(transaction: ParsedUpiTransaction) {
        viewModelScope.launch {
            val expenseId = UUID.randomUUID().toString()
            val expense = Expense(
                id = expenseId,
                groupId = null,
                description = transaction.merchantOrSender,
                totalAmount = transaction.amount,
                paidByUserId = "user_me",
                category = transaction.category.displayName,
                isPersonal = true,
                timestamp = transaction.timestamp,
                splits = listOf(ExpenseSplit(expenseId, "user_me", transaction.amount))
            )
            repository.addExpense(expense)
            _uiState.value = _uiState.value.copy(
                savedExpenseIds = _uiState.value.savedExpenseIds + transaction.rawSms
            )
        }
    }

    private fun groupTransactionsByDay(transactions: List<ParsedUpiTransaction>): List<DailyUpiGroup> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())

        val map = mutableMapOf<String, MutableList<ParsedUpiTransaction>>()
        val timestampMap = mutableMapOf<String, Long>()

        for (tx in transactions) {
            val dateKey = dateFormat.format(Date(tx.timestamp))
            map.getOrPut(dateKey) { mutableListOf() }.add(tx)
            if (!timestampMap.containsKey(dateKey)) {
                timestampMap[dateKey] = tx.timestamp
            }
        }

        val todayKey = dateFormat.format(Date())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = dateFormat.format(cal.time)

        return map.entries.map { (dateKey, txList) ->
            val label = when (dateKey) {
                todayKey -> "Today"
                yesterdayKey -> "Yesterday"
                else -> displayFormat.format(Date(timestampMap[dateKey] ?: 0L))
            }
            val dailyTotal = txList.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount }
            DailyUpiGroup(
                dateLabel = label,
                dateTimestamp = timestampMap[dateKey] ?: 0L,
                dailyExpenseTotal = dailyTotal,
                transactions = txList
            )
        }.sortedByDescending { it.dateTimestamp }
    }
}
