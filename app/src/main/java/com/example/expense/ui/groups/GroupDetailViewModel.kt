package com.example.expense.ui.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense.domain.model.Expense
import com.example.expense.domain.model.Group
import com.example.expense.domain.model.Settlement
import com.example.expense.domain.model.User
import com.example.expense.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class GroupDetailUiState(
    val group: Group? = null,
    val members: List<User> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val settlements: List<Settlement> = emptyList()
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ExpenseRepository
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    val uiState: StateFlow<GroupDetailUiState> = combine(
        repository.getGroups(),
        repository.getGroupMembers(groupId),
        repository.getExpensesByGroup(groupId),
        repository.getSettlementsByGroup(groupId)
    ) { groups, members, expenses, settlements ->
        val group = groups.find { it.id == groupId }
        GroupDetailUiState(
            group = group,
            members = members,
            expenses = expenses,
            settlements = settlements
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupDetailUiState()
    )
}
