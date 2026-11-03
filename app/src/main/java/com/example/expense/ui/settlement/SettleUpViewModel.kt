package com.example.expense.ui.settlement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expense.domain.model.Settlement
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

data class SettleUpUiState(
    val payerId: String = "user_me",
    val receiverId: String = "",
    val amountText: String = "",
    val availableUsers: List<User> = emptyList(),
    val isSaved: Boolean = false
)

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettleUpUiState())
    val uiState: StateFlow<SettleUpUiState> = combine(
        _uiState,
        repository.getUsers()
    ) { state, users ->
        val defaultReceiver = if (state.receiverId.isBlank() && users.size > 1) {
            users.firstOrNull { it.id != state.payerId }?.id ?: ""
        } else state.receiverId

        state.copy(
            availableUsers = users,
            receiverId = defaultReceiver
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettleUpUiState()
    )

    fun onPayerChange(payerId: String) {
        _uiState.value = _uiState.value.copy(payerId = payerId)
    }

    fun onReceiverChange(receiverId: String) {
        _uiState.value = _uiState.value.copy(receiverId = receiverId)
    }

    fun onAmountChange(amountText: String) {
        _uiState.value = _uiState.value.copy(amountText = amountText)
    }

    fun saveSettlement(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        val amount = currentState.amountText.toDoubleOrNull() ?: return
        if (amount <= 0 || currentState.payerId == currentState.receiverId || currentState.receiverId.isBlank()) return

        viewModelScope.launch {
            val settlement = Settlement(
                id = UUID.randomUUID().toString(),
                groupId = null,
                payerId = currentState.payerId,
                receiverId = currentState.receiverId,
                amount = amount,
                timestamp = System.currentTimeMillis()
            )
            repository.addSettlement(settlement)
            _uiState.value = _uiState.value.copy(isSaved = true)
            onSuccess()
        }
    }
}
