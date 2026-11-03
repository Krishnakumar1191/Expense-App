package com.example.expense.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense.domain.model.Expense
import com.example.expense.domain.model.ExpenseCategory
import com.example.expense.domain.model.ExpenseSplit
import com.example.expense.domain.model.Group
import com.example.expense.domain.model.User
import com.example.expense.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddExpenseUiState(
    val description: String = "",
    val amountText: String = "",
    val paidByUserId: String = "user_me",
    val selectedCategory: ExpenseCategory = ExpenseCategory.FOOD,
    val isPersonal: Boolean = false,
    val selectedGroupId: String? = null,
    val selectedParticipantIds: Set<String> = emptySet(),
    val availableUsers: List<User> = emptyList(),
    val availableGroups: List<Group> = emptyList(),
    val isSaved: Boolean = false
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = combine(
        _uiState,
        repository.getUsers(),
        repository.getGroups()
    ) { state, users, groups ->
        state.copy(
            availableUsers = users,
            availableGroups = groups,
            selectedParticipantIds = if (state.selectedParticipantIds.isEmpty() && users.isNotEmpty()) {
                users.map { it.id }.toSet()
            } else state.selectedParticipantIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AddExpenseUiState()
    )

    fun onDescriptionChange(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onAmountChange(amountText: String) {
        _uiState.value = _uiState.value.copy(amountText = amountText)
    }

    fun onCategoryChange(category: ExpenseCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onPersonalToggle(isPersonal: Boolean) {
        _uiState.value = _uiState.value.copy(isPersonal = isPersonal)
    }

    fun onPaidByUserChange(userId: String) {
        _uiState.value = _uiState.value.copy(paidByUserId = userId)
    }

    fun onGroupChange(groupId: String?) {
        _uiState.value = _uiState.value.copy(selectedGroupId = groupId)
    }

    fun toggleParticipant(userId: String) {
        val current = _uiState.value.selectedParticipantIds.toMutableSet()
        if (current.contains(userId)) {
            if (current.size > 1) current.remove(userId)
        } else {
            current.add(userId)
        }
        _uiState.value = _uiState.value.copy(selectedParticipantIds = current)
    }

    fun saveExpense(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val amount = currentState.amountText.toDoubleOrNull() ?: return
        if (currentState.description.isBlank() || amount <= 0) return

        viewModelScope.launch {
            val expenseId = UUID.randomUUID().toString()

            val participantIds = if (currentState.isPersonal) {
                setOf(currentState.paidByUserId)
            } else {
                currentState.selectedParticipantIds.ifEmpty { setOf(currentState.paidByUserId) }
            }

            val splitAmount = amount / participantIds.size

            val splits = participantIds.map { userId ->
                ExpenseSplit(
                    expenseId = expenseId,
                    userId = userId,
                    amountOwed = splitAmount
                )
            }

            val expense = Expense(
                id = expenseId,
                groupId = if (currentState.isPersonal) null else currentState.selectedGroupId,
                description = currentState.description,
                totalAmount = amount,
                paidByUserId = currentState.paidByUserId,
                category = currentState.selectedCategory.displayName,
                isPersonal = currentState.isPersonal,
                timestamp = System.currentTimeMillis(),
                splits = splits
            )

            repository.addExpense(expense)
            _uiState.value = _uiState.value.copy(isSaved = true)
            onSuccess()
        }
    }
}
