package com.example.expense.domain.repository

import com.example.expense.domain.model.Account
import com.example.expense.domain.model.CategoryBudget
import com.example.expense.domain.model.Expense
import com.example.expense.domain.model.Group
import com.example.expense.domain.model.Income
import com.example.expense.domain.model.Settlement
import com.example.expense.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getUsers(): Flow<List<User>>
    suspend fun getUserById(userId: String): User?
    suspend fun addUser(user: User)
    suspend fun addUsers(users: List<User>)

    fun getGroups(): Flow<List<Group>>
    fun getGroupMembers(groupId: String): Flow<List<User>>
    suspend fun addGroup(group: Group, memberIds: List<String>)

    fun getExpenses(): Flow<List<Expense>>
    fun getExpensesByGroup(groupId: String): Flow<List<Expense>>
    suspend fun addExpense(expense: Expense)

    fun getSettlements(): Flow<List<Settlement>>
    fun getSettlementsByGroup(groupId: String): Flow<List<Settlement>>
    suspend fun addSettlement(settlement: Settlement)

    // Wallet Accounts
    fun getAccounts(): Flow<List<Account>>
    suspend fun addAccount(account: Account)
    suspend fun updateAccountBalance(accountId: String, amountDelta: Double)

    // Incomes / Salary
    fun getIncomes(): Flow<List<Income>>
    suspend fun addIncome(income: Income)

    // Category Budgets
    fun getCategoryBudgets(): Flow<List<CategoryBudget>>
    suspend fun setCategoryBudget(categoryName: String, monthlyLimit: Double)

    suspend fun purgeSampleData()
}
