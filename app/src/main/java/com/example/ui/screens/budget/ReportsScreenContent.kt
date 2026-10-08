package com.example.ui.screens.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.PeriodType
import com.example.ui.components.FinancialSummaryCard
import com.example.ui.components.HighestSpendingCard
import com.example.ui.components.IncomeExpenseChart
import com.example.ui.components.MonthSelector
import com.example.ui.components.PeriodSelector
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.BudgetViewModel
import com.example.ui.viewmodel.ReportsUiState

@Composable
fun ReportsScreenContent(
    viewModel: BudgetViewModel,
    selectedMonth: String,
    selectedPeriod: PeriodType,
    reportsState: ReportsUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_analytics_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Selector
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

        // Period Selector: Daily | Weekly | Monthly
        item {
            PeriodSelector(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { period -> viewModel.setPeriod(period) }
            )
        }

        // Chart: Income vs. expense
        item {
            when (reportsState) {
                is ReportsUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = TealPrimary)
                    }
                }
                is ReportsUiState.Success -> {
                    IncomeExpenseChart(data = reportsState.report.incomeExpenseData)
                }
                is ReportsUiState.Error -> {
                    Text(
                        text = "Unable to load chart data",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Highest spending category card
        item {
            when (reportsState) {
                is ReportsUiState.Success -> {
                    HighestSpendingCard(
                        category = reportsState.report.highestCategory,
                        spentFormatted = "PKR ${reportsState.report.highestCategoryAmount.toLong()} spent this month",
                        percentage = reportsState.report.highestCategoryPercentage
                    )
                }
                else -> {
                    HighestSpendingCard(
                        category = "Shopping",
                        spentFormatted = "PKR 8,250 spent this month",
                        percentage = 24
                    )
                }
            }
        }

        // Financial Summary card
        item {
            when (reportsState) {
                is ReportsUiState.Success -> {
                    FinancialSummaryCard(
                        totalIncome = reportsState.report.totalIncome,
                        totalExpenses = reportsState.report.totalExpenses,
                        netSavings = reportsState.report.netSavings
                    )
                }
                else -> {
                    FinancialSummaryCard(
                        totalIncome = 120000.0,
                        totalExpenses = 35750.0,
                        netSavings = 84250.0
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
