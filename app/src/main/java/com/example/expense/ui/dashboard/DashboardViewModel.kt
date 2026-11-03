package com.example.expense.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense.domain.model.Debt
import com.example.expense.domain.model.Expense
import com.example.expense.domain.model.ExpenseCategory
import com.example.expense.domain.model.Group
import com.example.expense.domain.model.User
import com.example.expense.domain.repository.ExpenseRepository
import com.example.expense.domain.usecase.CalculateBalancesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val currentUser: User? = null,
    val totalYouAreOwed: Double = 0.0,
    val totalYouOwe: Double = 0.0,
    val netBalance: Double = 0.0,
    val simplifiedDebts: List<Debt> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val allExpensesForExport: List<Expense> = emptyList(),
    val categoryTotals: Map<ExpenseCategory, Double> = emptyMap(),
    val personalTotal: Double = 0.0,
    val groupTotal: Double = 0.0,
    val selectedTimeFilter: String = "This Month",
    val allUsers: List<User> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val calculateBalancesUseCase: CalculateBalancesUseCase
) : ViewModel() {

    val CURRENT_USER_ID = "user_me"
    private val _selectedTimeFilter = MutableStateFlow("This Month")

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.getUsers(),
        repository.getExpenses(),
        repository.getSettlements(),
        _selectedTimeFilter
    ) { users, expenses, settlements, timeFilter ->
        val otherUsers = users.filter { it.id != CURRENT_USER_ID }
        val currentUser = users.find { it.id == CURRENT_USER_ID }

        if (currentUser == null) {
            ensureCurrentUserExists()
            DashboardUiState(isLoading = true)
        } else {
            val netBalances = calculateBalancesUseCase.calculateNetBalances(users, expenses, settlements)
            val simplifiedDebts = calculateBalancesUseCase.calculateSimplifiedDebts(users, expenses, settlements)

            val myNetBalance = netBalances[currentUser.id] ?: 0.0

            var totalOwedToMe = 0.0
            var totalIOwe = 0.0

            for (debt in simplifiedDebts) {
                if (debt.toUser.id == currentUser.id) {
                    totalOwedToMe += debt.amount
                } else if (debt.fromUser.id == currentUser.id) {
                    totalIOwe += debt.amount
                }
            }

            val filteredExpenses = filterExpensesByTime(expenses, timeFilter)

            val categoryMap = ExpenseCategory.entries.associateWith { category ->
                filteredExpenses.filter { ExpenseCategory.fromName(it.category) == category }.sumOf { it.totalAmount }
            }

            val personalTotal = filteredExpenses.filter { it.isPersonal }.sumOf { it.totalAmount }
            val groupTotal = filteredExpenses.filter { !it.isPersonal }.sumOf { it.totalAmount }

            DashboardUiState(
                currentUser = currentUser,
                totalYouAreOwed = totalOwedToMe,
                totalYouOwe = totalIOwe,
                netBalance = myNetBalance,
                simplifiedDebts = simplifiedDebts,
                recentExpenses = filteredExpenses.take(10),
                allExpensesForExport = filteredExpenses,
                categoryTotals = categoryMap,
                personalTotal = personalTotal,
                groupTotal = groupTotal,
                selectedTimeFilter = timeFilter,
                allUsers = users,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun onTimeFilterSelected(filter: String) {
        _selectedTimeFilter.value = filter
    }

    private fun filterExpensesByTime(expenses: List<Expense>, filter: String): List<Expense> {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        return when (filter) {
            "This Month" -> expenses.filter {
                cal.timeInMillis = it.timestamp
                cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
            }
            "Last Month" -> expenses.filter {
                cal.timeInMillis = it.timestamp
                val lastMonth = if (currentMonth == 0) 11 else currentMonth - 1
                val targetYear = if (currentMonth == 0) currentYear - 1 else currentYear
                cal.get(Calendar.MONTH) == lastMonth && cal.get(Calendar.YEAR) == targetYear
            }
            else -> expenses
        }
    }

    private fun ensureCurrentUserExists() {
        viewModelScope.launch {
            repository.purgeSampleData()
            val me = User(CURRENT_USER_ID, "You", "you@example.com")
            repository.addUser(me)
        }
    }
}
