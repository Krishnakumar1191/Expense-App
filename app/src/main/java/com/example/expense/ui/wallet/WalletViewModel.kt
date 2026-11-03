package com.example.expense.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense.domain.model.Account
import com.example.expense.domain.model.CategoryBudget
import com.example.expense.domain.model.ExpenseCategory
import com.example.expense.domain.model.Income
import com.example.expense.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

data class WalletUiState(
    val accounts: List<Account> = emptyList(),
    val totalWalletBalance: Double = 0.0,
    val monthlyIncomes: List<Income> = emptyList(),
    val totalMonthlySalary: Double = 0.0,
    val totalMonthlyExpenses: Double = 0.0,
    val remainingSalary: Double = 0.0,
    val salarySpentPercentage: Int = 0,
    val categoryBudgets: List<CategoryBudget> = emptyList()
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val uiState: StateFlow<WalletUiState> = combine(
        repository.getAccounts(),
        repository.getIncomes(),
        repository.getExpenses(),
        repository.getCategoryBudgets()
    ) { accounts, incomes, expenses, budgets ->
        if (accounts.isEmpty()) {
            ensureDefaultAccount()
        }

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        val thisMonthIncomes = incomes.filter {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }

        val thisMonthExpenses = expenses.filter {
            cal.timeInMillis = it.timestamp
            cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
        }

        val totalIncome = thisMonthIncomes.sumOf { it.amount }
        val totalExpense = thisMonthExpenses.sumOf { it.totalAmount }
        val remainingSalary = totalIncome - totalExpense
        val pctSpent = if (totalIncome > 0) ((totalExpense / totalIncome) * 100).toInt() else 0

        val categorySpentMap = ExpenseCategory.entries.associateWith { cat ->
            thisMonthExpenses.filter { ExpenseCategory.fromName(it.category) == cat }.sumOf { it.totalAmount }
        }

        val enrichedBudgets = ExpenseCategory.entries.map { cat ->
            val limit = budgets.find { it.categoryName == cat.displayName }?.monthlyLimit ?: 5000.0
            val spent = categorySpentMap[cat] ?: 0.0
            CategoryBudget(categoryName = cat.displayName, monthlyLimit = limit, currentSpent = spent)
        }

        val totalBalance = accounts.sumOf { it.balance }

        WalletUiState(
            accounts = accounts,
            totalWalletBalance = totalBalance,
            monthlyIncomes = thisMonthIncomes,
            totalMonthlySalary = totalIncome,
            totalMonthlyExpenses = totalExpense,
            remainingSalary = remainingSalary,
            salarySpentPercentage = pctSpent,
            categoryBudgets = enrichedBudgets
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WalletUiState()
    )

    fun addIncome(source: String, amount: Double) {
        if (amount <= 0 || source.isBlank()) return
        viewModelScope.launch {
            val income = Income(
                id = UUID.randomUUID().toString(),
                source = source,
                amount = amount,
                accountId = "acc_primary",
                timestamp = System.currentTimeMillis()
            )
            repository.addIncome(income)
        }
    }

    fun setBudget(categoryName: String, monthlyLimit: Double) {
        if (monthlyLimit < 0) return
        viewModelScope.launch {
            repository.setCategoryBudget(categoryName, monthlyLimit)
        }
    }

    private fun ensureDefaultAccount() {
        viewModelScope.launch {
            val defaultAccount = Account(
                id = "acc_primary",
                name = "Primary Bank (UPI)",
                type = "BANK",
                balance = 0.0
            )
            val cashAccount = Account(
                id = "acc_cash",
                name = "Cash Wallet",
                type = "CASH",
                balance = 0.0
            )
            repository.addAccount(defaultAccount)
            repository.addAccount(cashAccount)
        }
    }
}
