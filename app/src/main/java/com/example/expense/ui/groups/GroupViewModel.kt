package com.example.expense.ui.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense.domain.model.Group
import com.example.expense.domain.model.User
import com.example.expense.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class GroupUiState(
    val groups: List<Group> = emptyList(),
    val availableUsers: List<User> = emptyList()
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    val uiState: StateFlow<GroupUiState> = combine(
        repository.getGroups(),
        repository.getUsers()
    ) { groups, users ->
        GroupUiState(groups = groups, availableUsers = users)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupUiState()
    )

    fun createGroup(name: String, memberIds: List<String>) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val group = Group(
                id = UUID.randomUUID().toString(),
                name = name,
                createdAt = System.currentTimeMillis()
            )
            val finalMembers = if (memberIds.isEmpty()) listOf("user_me") else memberIds
            repository.addGroup(group, finalMembers)
        }
    }
}
