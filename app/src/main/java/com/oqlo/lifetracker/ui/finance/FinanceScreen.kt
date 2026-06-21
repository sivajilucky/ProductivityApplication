package com.oqlo.lifetracker.ui.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oqlo.lifetracker.data.finance.TransactionEntity
import com.oqlo.lifetracker.data.finance.TransactionType
import com.oqlo.lifetracker.ui.common.AnimatedTrendChart
import com.oqlo.lifetracker.ui.common.BarData
import java.time.format.TextStyle
import java.time.YearMonth
import java.util.Locale

@Composable
fun FinanceScreen(viewModel: FinanceViewModel = viewModel()) {
    val transactions by viewModel.allTransactions.collectAsState()
    val budgetGoal by viewModel.budgetGoal.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    val thisMonth = remember(transactions) { viewModel.monthSummary(transactions, YearMonth.now()) }
    val lastMonth = remember(transactions) { viewModel.monthSummary(transactions, YearMonth.now().minusMonths(1)) }
    val expenseTrend = remember(transactions) { viewModel.weeklyExpenseTrend(transactions) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add transaction")
            }
        }
    ) { padding ->
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Text("Finance", style = MaterialTheme.typography.headlineSmall)
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.AccountBalanceWallet, contentDescription = "Finance", tint = Color(0xFF2E7D32))
                            Text("This Month", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                        }
                        Text(
                            "Income ₹${"%.0f".format(thisMonth.income)}  /  Expense ₹${"%.0f".format(thisMonth.expense)}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            "Net savings: ₹${"%.0f".format(thisMonth.netSavings)}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                        if (expenseTrend.any { it.second > 0.0 }) {
                            Text("Last 7 days spend", style = MaterialTheme.typography.labelSmall)
                            AnimatedTrendChart(
                                data = expenseTrend.map { (day, value) ->
                                    BarData(
                                        day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2),
                                        value.toFloat(),
                                        Color(0xFFD32F2F)
                                    )
                                },
                                barHeight = 70.dp,
                                valueLabel = { "₹${it.toInt()}" }
                            )
                        }
                    }
                }
            }
            budgetGoal?.let { goal ->
                item {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Savings, contentDescription = "Savings goal", tint = Color(0xFF6750A4))
                                Text(
                                    "Savings goal: ₹${"%.0f".format(goal.monthlySavingsGoal)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            val progress = if (goal.monthlySavingsGoal > 0)
                                (thisMonth.netSavings / goal.monthlySavingsGoal).toFloat().coerceIn(0f, 1f) else 0f
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
            item {
                Button(onClick = { showGoalDialog = true }) { Text("Set budget / savings goal") }
            }
            item { InsightCard(thisMonth, lastMonth) }
            item { Text("Recent transactions", style = MaterialTheme.typography.titleMedium) }
            items(transactions.take(50)) { transaction ->
                TransactionRow(transaction, onCategoryChange = { viewModel.confirmCategory(transaction, it) })
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { amount, type, merchant, category ->
                viewModel.addManualTransaction(amount, type, merchant, category)
                showAddDialog = false
            }
        )
    }

    if (showGoalDialog) {
        SetGoalDialog(
            onDismiss = { showGoalDialog = false },
            onSave = { budget, goal ->
                viewModel.setBudgetGoal(budget, goal)
                showGoalDialog = false
            }
        )
    }
}

@Composable
private fun InsightCard(thisMonth: MonthSummary, lastMonth: MonthSummary) {
    val insights = buildList {
        thisMonth.expenseByCategory.forEach { (category, amount) ->
            val lastAmount = lastMonth.expenseByCategory[category] ?: 0.0
            if (lastAmount > 0) {
                val changePct = ((amount - lastAmount) / lastAmount * 100)
                if (changePct > 15) {
                    add("You spent ${"%.0f".format(changePct)}% more on $category this month vs last month.")
                }
            }
        }
        if (thisMonth.expense > thisMonth.income && thisMonth.income > 0) {
            add("Expenses exceeded income this month — consider reviewing your top categories.")
        }
    }

    if (insights.isNotEmpty()) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = "Insights", tint = Color(0xFFEF6C00))
                    Text("Insights", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 8.dp))
                }
                insights.forEach { Text("• $it", modifier = Modifier.padding(top = 4.dp)) }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: TransactionEntity, onCategoryChange: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(transaction.merchant, style = MaterialTheme.typography.bodyLarge)
                    Text(transaction.category, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    (if (transaction.type == TransactionType.DEBIT) "-₹" else "+₹") + "%.0f".format(transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (transaction.type == TransactionType.DEBIT) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                )
            }
            if (transaction.category == "Uncategorized") {
                Button(onClick = { editing = true }, modifier = Modifier.padding(top = 4.dp)) { Text("Fix category") }
            }
        }
    }
    if (editing) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { editing = false },
            confirmButton = {
                Button(onClick = { onCategoryChange(text); editing = false }) { Text("Save") }
            },
            title = { Text("Set category") },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Category") }) }
        )
    }
}

@Composable
private fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (Double, TransactionType, String, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isDebit by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add transaction") },
        text = {
            Column {
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount") })
                OutlinedTextField(value = merchant, onValueChange = { merchant = it }, label = { Text("Merchant / Source") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (optional)") })
                Row {
                    Button(onClick = { isDebit = true }) { Text(if (isDebit) "● Debit" else "Debit") }
                    Button(onClick = { isDebit = false }) { Text(if (!isDebit) "● Credit" else "Credit") }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsedAmount = amount.toDoubleOrNull() ?: return@Button
                onSave(parsedAmount, if (isDebit) TransactionType.DEBIT else TransactionType.CREDIT, merchant, category)
            }) { Text("Save") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SetGoalDialog(onDismiss: () -> Unit, onSave: (Double, Double) -> Unit) {
    var budget by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set monthly budget & savings goal") },
        text = {
            Column {
                OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Monthly budget") })
                OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text("Savings goal") })
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(budget.toDoubleOrNull() ?: 0.0, goal.toDoubleOrNull() ?: 0.0)
            }) { Text("Save") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}
