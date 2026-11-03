package com.example.expense.ui.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense.domain.model.User
import com.example.expense.domain.repository.ExpenseRepository
import com.example.expense.domain.usecase.CalculateBalancesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class FriendBalanceItem(
    val user: User,
    val netBalance: Double
)

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val calculateBalancesUseCase: CalculateBalancesUseCase
) : ViewModel() {

    val CURRENT_USER_ID = "user_me"

    val friends: StateFlow<List<FriendBalanceItem>> = combine(
        repository.getUsers(),
        repository.getExpenses(),
        repository.getSettlements()
    ) { users, expenses, settlements ->
        val otherUsers = users.filter { it.id != CURRENT_USER_ID }
        val simplifiedDebts = calculateBalancesUseCase.calculateSimplifiedDebts(users, expenses, settlements)

        otherUsers.map { friend ->
            var friendNetBalance = 0.0
            for (debt in simplifiedDebts) {
                if (debt.fromUser.id == friend.id && debt.toUser.id == CURRENT_USER_ID) {
                    friendNetBalance += debt.amount
                } else if (debt.fromUser.id == CURRENT_USER_ID && debt.toUser.id == friend.id) {
                    friendNetBalance -= debt.amount
                }
            }
            FriendBalanceItem(friend, friendNetBalance)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addFriend(name: String, email: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val friend = User(
                id = UUID.randomUUID().toString(),
                name = name,
                email = email.ifBlank { "${name.lowercase().replace(" ", "")}@example.com" }
            )
            repository.addUser(friend)
        }
    }
}
