package com.example.expense.ui.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    var showAddIncomeDialog by remember { mutableStateOf(false) }
    var incomeSourceInput by remember { mutableStateOf("Monthly Salary") }
    var incomeAmountInput by remember { mutableStateOf("") }

    var editingCategory by remember { mutableStateOf<String?>(null) }
    var budgetInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet & Salary Budget", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Monthly Salary & Income", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    currencyFormat.format(uiState.totalMonthlySalary),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(onClick = { showAddIncomeDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Salary")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Salary Spent: ${uiState.salarySpentPercentage}% (${currencyFormat.format(uiState.totalMonthlyExpenses)} of ${currencyFormat.format(uiState.totalMonthlySalary)})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (uiState.totalMonthlySalary > 0) (uiState.totalMonthlyExpenses / uiState.totalMonthlySalary).toFloat().coerceIn(0f, 1f) else 0f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = if (uiState.salarySpentPercentage > 85) Color(0xFFC62828) else MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Remaining Balance: ${currencyFormat.format(uiState.remainingSalary)}",
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.remainingSalary >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            item {
                Text("Accounts & Wallet Balances", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(uiState.accounts) { account ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (account.type == "CASH") Icons.Default.Wallet else Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(account.name, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            currencyFormat.format(account.balance),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (account.balance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            item {
                Text("Category Monthly Budgets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(uiState.categoryBudgets) { budget ->
                val spentPct = if (budget.monthlyLimit > 0) ((budget.currentSpent / budget.monthlyLimit) * 100).toInt() else 0
                val isOver = budget.currentSpent > budget.monthlyLimit

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(budget.categoryName, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                editingCategory = budget.categoryName
                                budgetInput = budget.monthlyLimit.toString()
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Set Budget", modifier = Modifier.size(18.dp))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Spent: ${currencyFormat.format(budget.currentSpent)} / Limit: ${currencyFormat.format(budget.monthlyLimit)}",
                                fontSize = 12.sp,
                                color = if (isOver) Color(0xFFC62828) else Color.Gray
                            )
                            Text("$spentPct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isOver) Color(0xFFC62828) else Color(0xFF2E7D32))
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { if (budget.monthlyLimit > 0) (budget.currentSpent / budget.monthlyLimit).toFloat().coerceIn(0f, 1f) else 0f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = if (isOver) Color(0xFFC62828) else Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }

        if (showAddIncomeDialog) {
            AlertDialog(
                onDismissRequest = { showAddIncomeDialog = false },
                title = { Text("Add Salary / Income") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = incomeSourceInput,
                            onValueChange = { incomeSourceInput = it },
                            label = { Text("Source (e.g. Monthly Salary, Freelance)") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = incomeAmountInput,
                            onValueChange = { incomeAmountInput = it },
                            label = { Text("Amount (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val amt = incomeAmountInput.toDoubleOrNull() ?: 0.0
                            if (amt > 0 && incomeSourceInput.isNotBlank()) {
                                viewModel.addIncome(incomeSourceInput, amt)
                                incomeAmountInput = ""
                                showAddIncomeDialog = false
                            }
                        }
                    ) {
                        Text("Add Income")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddIncomeDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (editingCategory != null) {
            AlertDialog(
                onDismissRequest = { editingCategory = null },
                title = { Text("Set Monthly Budget for $editingCategory") },
                text = {
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("Monthly Limit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val limit = budgetInput.toDoubleOrNull() ?: 0.0
                            if (limit >= 0 && editingCategory != null) {
                                viewModel.setBudget(editingCategory!!, limit)
                                editingCategory = null
                            }
                        }
                    ) {
                        Text("Save Budget")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { editingCategory = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
