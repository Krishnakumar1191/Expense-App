package com.example.expense.data.repository

import com.example.expense.data.local.dao.ExpenseDao
import com.example.expense.data.local.entity.AccountEntity
import com.example.expense.data.local.entity.CategoryBudgetEntity
import com.example.expense.data.local.entity.ExpenseEntity
import com.example.expense.data.local.entity.ExpenseSplitEntity
import com.example.expense.data.local.entity.GroupEntity
import com.example.expense.data.local.entity.GroupMemberEntity
import com.example.expense.data.local.entity.IncomeEntity
import com.example.expense.data.local.entity.SettlementEntity
import com.example.expense.data.local.entity.UserEntity
import com.example.expense.domain.model.Account
import com.example.expense.domain.model.CategoryBudget
import com.example.expense.domain.model.Expense
import com.example.expense.domain.model.ExpenseSplit
import com.example.expense.domain.model.Group
import com.example.expense.domain.model.Income
import com.example.expense.domain.model.Settlement
import com.example.expense.domain.model.User
import com.example.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getUsers(): Flow<List<User>> {
        return dao.getAllUsers().map { entities ->
            entities.map { User(it.id, it.name, it.email) }
        }
    }

    override suspend fun getUserById(userId: String): User? {
        return dao.getUserById(userId)?.let { User(it.id, it.name, it.email) }
    }

    override suspend fun addUser(user: User) {
        dao.insertUser(UserEntity(user.id, user.name, user.email))
    }

    override suspend fun addUsers(users: List<User>) {
        dao.insertUsers(users.map { UserEntity(it.id, it.name, it.email) })
    }

    override fun getGroups(): Flow<List<Group>> {
        return dao.getAllGroups().map { entities ->
            entities.map { Group(it.id, it.name, it.createdAt) }
        }
    }

    override fun getGroupMembers(groupId: String): Flow<List<User>> {
        return dao.getGroupMembers(groupId).map { entities ->
            entities.map { User(it.id, it.name, it.email) }
        }
    }

    override suspend fun addGroup(group: Group, memberIds: List<String>) {
        dao.insertGroup(GroupEntity(group.id, group.name, group.createdAt))
        val members = memberIds.map { userId -> GroupMemberEntity(group.id, userId) }
        dao.insertGroupMembers(members)
    }

    override fun getExpenses(): Flow<List<Expense>> {
        return dao.getAllExpenses().map { entities ->
            entities.map { expenseEntity ->
                val splits = dao.getSplitsForExpense(expenseEntity.id).map {
                    ExpenseSplit(it.expenseId, it.userId, it.amountOwed)
                }
                Expense(
                    id = expenseEntity.id,
                    groupId = expenseEntity.groupId,
                    description = expenseEntity.description,
                    totalAmount = expenseEntity.totalAmount,
                    paidByUserId = expenseEntity.paidByUserId,
                    category = expenseEntity.category,
                    isPersonal = expenseEntity.isPersonal,
                    timestamp = expenseEntity.timestamp,
                    splits = splits
                )
            }
        }
    }

    override fun getExpensesByGroup(groupId: String): Flow<List<Expense>> {
        return dao.getExpensesByGroup(groupId).map { entities ->
            entities.map { expenseEntity ->
                val splits = dao.getSplitsForExpense(expenseEntity.id).map {
                    ExpenseSplit(it.expenseId, it.userId, it.amountOwed)
                }
                Expense(
                    id = expenseEntity.id,
                    groupId = expenseEntity.groupId,
                    description = expenseEntity.description,
                    totalAmount = expenseEntity.totalAmount,
                    paidByUserId = expenseEntity.paidByUserId,
                    category = expenseEntity.category,
                    isPersonal = expenseEntity.isPersonal,
                    timestamp = expenseEntity.timestamp,
                    splits = splits
                )
            }
        }
    }

    override suspend fun addExpense(expense: Expense) {
        val expenseEntity = ExpenseEntity(
            id = expense.id,
            groupId = expense.groupId,
            description = expense.description,
            totalAmount = expense.totalAmount,
            paidByUserId = expense.paidByUserId,
            category = expense.category,
            isPersonal = expense.isPersonal,
            timestamp = expense.timestamp
        )
        val splitEntities = expense.splits.map {
            ExpenseSplitEntity(
                expenseId = expense.id,
                userId = it.userId,
                amountOwed = it.amountOwed
            )
        }
        dao.insertExpense(expenseEntity)
        dao.insertExpenseSplits(splitEntities)

        if (expense.paidByUserId == "user_me") {
            dao.updateAccountBalance("acc_primary", -expense.totalAmount)
        }
    }

    override fun getSettlements(): Flow<List<Settlement>> {
        return dao.getAllSettlements().map { entities ->
            entities.map {
                Settlement(it.id, it.groupId, it.payerId, it.receiverId, it.amount, it.timestamp)
            }
        }
    }

    override fun getSettlementsByGroup(groupId: String): Flow<List<Settlement>> {
        return dao.getSettlementsByGroup(groupId).map { entities ->
            entities.map {
                Settlement(it.id, it.groupId, it.payerId, it.receiverId, it.amount, it.timestamp)
            }
        }
    }

    override suspend fun addSettlement(settlement: Settlement) {
        dao.insertSettlement(
            SettlementEntity(
                id = settlement.id,
                groupId = settlement.groupId,
                payerId = settlement.payerId,
                receiverId = settlement.receiverId,
                amount = settlement.amount,
                timestamp = settlement.timestamp
            )
        )

        if (settlement.payerId == "user_me") {
            dao.updateAccountBalance("acc_primary", -settlement.amount)
        } else if (settlement.receiverId == "user_me") {
            dao.updateAccountBalance("acc_primary", settlement.amount)
        }
    }

    override fun getAccounts(): Flow<List<Account>> {
        return dao.getAllAccounts().map { entities ->
            entities.map { Account(it.id, it.name, it.type, it.balance) }
        }
    }

    override suspend fun addAccount(account: Account) {
        dao.insertAccount(AccountEntity(account.id, account.name, account.type, account.balance))
    }

    override suspend fun updateAccountBalance(accountId: String, amountDelta: Double) {
        dao.updateAccountBalance(accountId, amountDelta)
    }

    override fun getIncomes(): Flow<List<Income>> {
        return dao.getAllIncomes().map { entities ->
            entities.map { Income(it.id, it.source, it.amount, it.accountId, it.timestamp) }
        }
    }

    override suspend fun addIncome(income: Income) {
        dao.insertIncome(IncomeEntity(income.id, income.source, income.amount, income.accountId, income.timestamp))
        dao.updateAccountBalance(income.accountId, income.amount)
    }

    override fun getCategoryBudgets(): Flow<List<CategoryBudget>> {
        return dao.getAllCategoryBudgets().map { entities ->
            entities.map { CategoryBudget(it.categoryName, it.monthlyLimit) }
        }
    }

    override suspend fun setCategoryBudget(categoryName: String, monthlyLimit: Double) {
        dao.insertCategoryBudget(CategoryBudgetEntity(categoryName, monthlyLimit))
    }

    override suspend fun purgeSampleData() {
        dao.purgeAllSampleData()
    }
}
