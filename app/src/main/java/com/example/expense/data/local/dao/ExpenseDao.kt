package com.example.expense.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.expense.data.local.entity.AccountEntity
import com.example.expense.data.local.entity.CategoryBudgetEntity
import com.example.expense.data.local.entity.ExpenseEntity
import com.example.expense.data.local.entity.ExpenseSplitEntity
import com.example.expense.data.local.entity.GroupEntity
import com.example.expense.data.local.entity.GroupMemberEntity
import com.example.expense.data.local.entity.IncomeEntity
import com.example.expense.data.local.entity.SettlementEntity
import com.example.expense.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    // Users
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>): List<Long>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    // Groups
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(members: List<GroupMemberEntity>): List<Long>

    @Query("SELECT * FROM app_groups")
    fun getAllGroups(): Flow<List<GroupEntity>>

    @Query("SELECT u.* FROM users u INNER JOIN group_members gm ON u.id = gm.userId WHERE gm.groupId = :groupId")
    fun getGroupMembers(groupId: String): Flow<List<UserEntity>>

    // Expenses & Splits
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseSplits(splits: List<ExpenseSplitEntity>): List<Long>

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getExpensesByGroup(groupId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense_splits")
    fun getAllExpenseSplits(): Flow<List<ExpenseSplitEntity>>

    @Query("SELECT * FROM expense_splits WHERE expenseId = :expenseId")
    suspend fun getSplitsForExpense(expenseId: String): List<ExpenseSplitEntity>

    // Settlements
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity): Long

    @Query("SELECT * FROM settlements ORDER BY timestamp DESC")
    fun getAllSettlements(): Flow<List<SettlementEntity>>

    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getSettlementsByGroup(groupId: String): Flow<List<SettlementEntity>>

    // Accounts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Query("SELECT * FROM accounts")
    fun getAllAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: String): AccountEntity?

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun updateAccountBalance(accountId: String, amount: Double)

    // Incomes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Query("SELECT * FROM incomes ORDER BY timestamp DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    // Category Budgets
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryBudget(budget: CategoryBudgetEntity): Long

    @Query("SELECT * FROM category_budgets")
    fun getAllCategoryBudgets(): Flow<List<CategoryBudgetEntity>>

    // Purge Sample Data
    @Query("DELETE FROM users WHERE id IN ('user_alex', 'user_sarah', 'user_mike')")
    suspend fun purgeSampleUsers()

    @Query("DELETE FROM app_groups WHERE id IN ('group_apt', 'group_trip')")
    suspend fun purgeSampleGroups()

    @Query("DELETE FROM expenses WHERE id IN ('exp_1', 'exp_2', 'exp_3')")
    suspend fun purgeSampleExpenses()

    @Transaction
    suspend fun purgeAllSampleData() {
        purgeSampleUsers()
        purgeSampleGroups()
        purgeSampleExpenses()
    }
}
