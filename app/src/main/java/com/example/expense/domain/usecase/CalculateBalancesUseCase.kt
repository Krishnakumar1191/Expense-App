package com.example.expense.domain.usecase

import com.example.expense.domain.model.Debt
import com.example.expense.domain.model.Expense
import com.example.expense.domain.model.Settlement
import com.example.expense.domain.model.User
import javax.inject.Inject

class CalculateBalancesUseCase @Inject constructor() {

    fun calculateNetBalances(
        users: List<User>,
        expenses: List<Expense>,
        settlements: List<Settlement>
    ): Map<String, Double> {
        val balances = users.associate { it.id to 0.0 }.toMutableMap()

        for (expense in expenses) {
            val payerId = expense.paidByUserId
            balances[payerId] = (balances[payerId] ?: 0.0) + expense.totalAmount

            for (split in expense.splits) {
                balances[split.userId] = (balances[split.userId] ?: 0.0) - split.amountOwed
            }
        }

        for (settlement in settlements) {
            balances[settlement.payerId] = (balances[settlement.payerId] ?: 0.0) + settlement.amount
            balances[settlement.receiverId] = (balances[settlement.receiverId] ?: 0.0) - settlement.amount
        }

        return balances
    }

    fun calculateSimplifiedDebts(
        users: List<User>,
        expenses: List<Expense>,
        settlements: List<Settlement>
    ): List<Debt> {
        val netBalances = calculateNetBalances(users, expenses, settlements)
        val userMap = users.associateBy { it.id }

        val debtors = mutableListOf<Pair<String, Double>>()
        val creditors = mutableListOf<Pair<String, Double>>()

        for ((userId, balance) in netBalances) {
            if (balance < -0.01) {
                debtors.add(userId to -balance)
            } else if (balance > 0.01) {
                creditors.add(userId to balance)
            }
        }

        val debts = mutableListOf<Debt>()
        var i = 0
        var j = 0

        while (i < debtors.size && j < creditors.size) {
            val (debtorId, debtAmount) = debtors[i]
            val (creditorId, creditAmount) = creditors[j]

            val settledAmount = minOf(debtAmount, creditAmount)

            val fromUser = userMap[debtorId]
            val toUser = userMap[creditorId]

            if (fromUser != null && toUser != null && settledAmount > 0.01) {
                debts.add(Debt(fromUser, toUser, settledAmount))
            }

            debtors[i] = debtorId to (debtAmount - settledAmount)
            creditors[j] = creditorId to (creditAmount - settledAmount)

            if (debtors[i].second < 0.01) i++
            if (creditors[j].second < 0.01) j++
        }

        return debts
    }
}
