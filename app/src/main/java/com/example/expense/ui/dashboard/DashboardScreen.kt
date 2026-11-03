package com.example.expense.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expense.ui.components.CategoryPieChart
import com.example.expense.ui.components.SpendingBarChart
import com.example.expense.util.ExcelExporter
import com.example.expense.util.FileDownloader
import com.example.expense.worker.NotificationScheduler
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToSettleUp: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        NotificationScheduler.scheduleWorkers(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(
                        onClick = {
                            if (uiState.allExpensesForExport.isEmpty()) {
                                Toast.makeText(context, "No expenses to export", Toast.LENGTH_SHORT).show()
                            } else {
                                val csv = ExcelExporter.generateCsvReport(
                                    monthName = uiState.selectedTimeFilter,
                                    expenses = uiState.allExpensesForExport,
                                    users = uiState.allUsers
                                )
                                val filename = "Expense_Report_${uiState.selectedTimeFilter.replace(" ", "_")}.csv"
                                FileDownloader.saveReportToDownloads(context, filename, csv)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Export Excel Report")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("This Month", "Last Month", "All Time").forEach { filter ->
                            FilterChip(
                                selected = uiState.selectedTimeFilter == filter,
                                onClick = { viewModel.onTimeFilterSelected(filter) },
                                label = { Text(filter) }
                            )
                        }
                    }
                }

                item {
                    SpendingBarChart(
                        personalTotal = uiState.personalTotal,
                        groupTotal = uiState.groupTotal
                    )
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Spending by Category", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            CategoryPieChart(categoryTotals = uiState.categoryTotals)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Total Net Balance", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            val balanceColor = when {
                                uiState.netBalance > 0.01 -> Color(0xFF2E7D32)
                                uiState.netBalance < -0.01 -> Color(0xFFC62828)
                                else -> Color.Gray
                            }
                            val balanceText = when {
                                uiState.netBalance > 0.01 -> "You are owed ${currencyFormat.format(uiState.netBalance)}"
                                uiState.netBalance < -0.01 -> "You owe ${currencyFormat.format(-uiState.netBalance)}"
                                else -> "You are all settled up!"
                            }
                            Text(
                                text = balanceText,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = balanceColor
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("You owe", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        currencyFormat.format(uiState.totalYouOwe),
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFC62828)
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(1.dp)
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("You are owed", fontSize = 12.sp, color = Color.Gray)
                                    Text(
                                        currencyFormat.format(uiState.totalYouAreOwed),
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToAddExpense,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Expense")
                        }
                        OutlinedButton(
                            onClick = onNavigateToSettleUp,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Settle Up")
                        }
                    }
                }

                if (uiState.simplifiedDebts.isNotEmpty()) {
                    item {
                        Text("Active Debts Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.simplifiedDebts) { debt ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${debt.fromUser.name} → ${debt.toUser.name}",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = currencyFormat.format(debt.amount),
                                    fontWeight = FontWeight.Bold,
                                    color = if (debt.toUser.id == viewModel.CURRENT_USER_ID) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }

                if (uiState.recentExpenses.isNotEmpty()) {
                    item {
                        Text("Recent Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(uiState.recentExpenses) { expense ->
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(expense.description, fontWeight = FontWeight.Bold)
                                        AssistChip(
                                            onClick = {},
                                            label = { Text(expense.category, fontSize = 10.sp) }
                                        )
                                    }
                                    Text(
                                        dateFormat.format(Date(expense.timestamp)),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    currencyFormat.format(expense.totalAmount),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
