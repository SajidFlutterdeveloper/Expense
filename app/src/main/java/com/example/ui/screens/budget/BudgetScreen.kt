package com.example.ui.screens.budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PeriodType
import com.example.ui.components.AppHeader
import com.example.ui.components.BudgetSummaryCard
import com.example.ui.components.BudgetWarningCard
import com.example.ui.components.CategorySpendingItem
import com.example.ui.components.MonthSelector
import com.example.ui.components.PeriodSelector
import com.example.ui.components.SpendingTrendChart
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.BudgetUiState
import com.example.ui.viewmodel.BudgetViewModel
import com.example.ui.viewmodel.ReportsUiState

enum class BudgetViewTab(val label: String) {
    OVERVIEW("Budget Overview"),
    ANALYTICS("Analytics & Reports")
}

@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    onNavigateToEditBudget: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val reportsState by viewModel.reportsState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    var activeViewTab by remember { mutableStateOf(BudgetViewTab.OVERVIEW) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("budget_screen")
    ) {
        // App Header
        AppHeader(
            title = "Budget & Reports",
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            notificationCount = 2
        )

        // Sub-tabs to seamlessly switch between Screen 9 and Screen 15
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
                .testTag("budget_sub_tab_selector"),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BudgetViewTab.entries.forEach { tab ->
                val isSelected = tab == activeViewTab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { activeViewTab = tab }
                        .testTag("sub_tab_${tab.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (tab == BudgetViewTab.OVERVIEW) Icons.Default.PieChart else Icons.Default.BarChart,
                            contentDescription = null,
                            tint = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (activeViewTab == BudgetViewTab.ANALYTICS) {
            // Render Screen 15 Reports / Analytics
            ReportsScreenContent(
                viewModel = viewModel,
                selectedMonth = selectedMonth,
                selectedPeriod = selectedPeriod,
                reportsState = reportsState
            )
        } else {
            // Render Screen 9 Budget & Reports Overview
            when (val state = uiState) {
                is BudgetUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("budget_loading_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = TealPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading budget...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is BudgetUiState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("budget_empty_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No budget set for this month.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onNavigateToEditBudget) {
                                Text("Create Budget")
                            }
                        }
                    }
                }

                is BudgetUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .testTag("budget_error_state"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            IconButton(onClick = { viewModel.loadBudgetData() }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                            }
                        }
                    }
                }

                is BudgetUiState.Success -> {
                    val budget = state.budget

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("budget_content_list"),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Month Selector: March 2025
                        item {
                            MonthSelector(
                                selectedMonth = selectedMonth,
                                onPreviousMonth = {
                                    val newMonth = if (selectedMonth == "March 2025") "February 2025" else "January 2025"
                                    viewModel.changeMonth(newMonth)
                                },
                                onNextMonth = {
                                    val newMonth = if (selectedMonth == "February 2025") "March 2025" else "April 2025"
                                    viewModel.changeMonth(newMonth)
                                }
                            )
                        }

                        // Monthly Budget Card
                        item {
                            BudgetSummaryCard(
                                budget = budget,
                                onEditBudgetClick = onNavigateToEditBudget
                            )
                        }

                        // Category Spending Section
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "Category spending",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Dynamic list of categories (Food, Transport, Shopping, Bills)
                                budget.categories.take(4).forEach { category ->
                                    CategorySpendingItem(
                                        category = category,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                }
                            }
                        }

                        // Warning Card: Bills are at 87% of your category budget.
                        item {
                            val billsCategory = budget.categories.find { it.name.equals("Bills", ignoreCase = true) }
                            val warningText = if (billsCategory != null) {
                                "${billsCategory.name} are at ${billsCategory.percentage}% of your category budget."
                            } else {
                                "Bills are at 87% of your category budget."
                            }
                            BudgetWarningCard(warningMessage = warningText)
                        }

                        // Segmented Selector: Daily | Weekly | Monthly
                        item {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                PeriodSelector(
                                    selectedPeriod = selectedPeriod,
                                    onPeriodSelected = { period -> viewModel.setPeriod(period) }
                                )
                            }
                        }

                        // Spending Trend Chart
                        item {
                            when (val rep = reportsState) {
                                is ReportsUiState.Success -> {
                                    SpendingTrendChart(points = rep.report.spendingTrendData)
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = TealPrimary)
                                    }
                                }
                            }
                        }

                        // Bottom spacer for navigation bar padding
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}
