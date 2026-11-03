package com.example.expense.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        UserEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class,
        ExpenseEntity::class,
        ExpenseSplitEntity::class,
        SettlementEntity::class,
        AccountEntity::class,
        IncomeEntity::class,
        CategoryBudgetEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
