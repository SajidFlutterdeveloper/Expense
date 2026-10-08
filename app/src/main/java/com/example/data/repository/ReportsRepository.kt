package com.example.data.repository

import com.example.data.model.FinancialReport
import com.example.data.model.IncomeExpensePoint
import com.example.data.model.PeriodType
import com.example.data.model.SpendingTrendPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ReportsRepository {
    val reportFlow: StateFlow<FinancialReport>
    suspend fun getReport(monthYear: String, period: PeriodType): Result<FinancialReport>
    suspend fun switchPeriod(period: PeriodType): FinancialReport
}

class ReportsRepositoryImpl : ReportsRepository {

    private fun generateReportFor(monthYear: String, period: PeriodType): FinancialReport {
        return when (period) {
            PeriodType.DAILY -> {
                val trend = listOf(
                    SpendingTrendPoint("Mon", 1200.0),
                    SpendingTrendPoint("Tue", 2400.0),
                    SpendingTrendPoint("Wed", 1850.0),
                    SpendingTrendPoint("Thu", 3100.0),
                    SpendingTrendPoint("Fri", 4200.0),
                    SpendingTrendPoint("Sat", 6800.0),
                    SpendingTrendPoint("Sun", 5200.0)
                )
                val incomeExpense = listOf(
                    IncomeExpensePoint("Mon", 0.0, 1200.0),
                    IncomeExpensePoint("Tue", 0.0, 2400.0),
                    IncomeExpensePoint("Wed", 40000.0, 1850.0),
                    IncomeExpensePoint("Thu", 0.0, 3100.0),
                    IncomeExpensePoint("Fri", 0.0, 4200.0),
                    IncomeExpensePoint("Sat", 0.0, 6800.0),
                    IncomeExpensePoint("Sun", 0.0, 5200.0)
                )
                FinancialReport(
                    monthYear = monthYear,
                    selectedPeriod = period,
                    totalIncome = 40000.0,
                    totalExpenses = 24750.0,
                    netSavings = 15250.0,
                    highestCategory = "Shopping",
                    highestCategoryAmount = 4800.0,
                    highestCategoryPercentage = 19,
                    spendingTrendData = trend,
                    incomeExpenseData = incomeExpense
                )
            }
            PeriodType.WEEKLY -> {
                val trend = listOf(
                    SpendingTrendPoint("W1", 8200.0),
                    SpendingTrendPoint("W2", 11450.0),
                    SpendingTrendPoint("W3", 9300.0),
                    SpendingTrendPoint("W4", 6800.0)
                )
                val incomeExpense = listOf(
                    IncomeExpensePoint("W1", 30000.0, 8200.0),
                    IncomeExpensePoint("W2", 30000.0, 11450.0),
                    IncomeExpensePoint("W3", 30000.0, 9300.0),
                    IncomeExpensePoint("W4", 30000.0, 6800.0)
                )
                FinancialReport(
                    monthYear = monthYear,
                    selectedPeriod = period,
                    totalIncome = 120000.0,
                    totalExpenses = 35750.0,
                    netSavings = 84250.0,
                    highestCategory = "Food",
                    highestCategoryAmount = 9450.0,
                    highestCategoryPercentage = 26,
                    spendingTrendData = trend,
                    incomeExpenseData = incomeExpense
                )
            }
            PeriodType.MONTHLY -> {
                val trend = listOf(
                    SpendingTrendPoint("Nov", 31200.0),
                    SpendingTrendPoint("Dec", 44500.0),
                    SpendingTrendPoint("Jan", 33800.0),
                    SpendingTrendPoint("Feb", 38900.0),
                    SpendingTrendPoint("Mar", 35750.0)
                )
                val incomeExpense = listOf(
                    IncomeExpensePoint("Nov", 110000.0, 31200.0),
                    IncomeExpensePoint("Dec", 130000.0, 44500.0),
                    IncomeExpensePoint("Jan", 115000.0, 33800.0),
                    IncomeExpensePoint("Feb", 118000.0, 38900.0),
                    IncomeExpensePoint("Mar", 120000.0, 35750.0)
                )
                FinancialReport(
                    monthYear = monthYear,
                    selectedPeriod = period,
                    totalIncome = 120000.0,
                    totalExpenses = 35750.0,
                    netSavings = 84250.0,
                    highestCategory = "Shopping",
                    highestCategoryAmount = 8250.0,
                    highestCategoryPercentage = 24,
                    spendingTrendData = trend,
                    incomeExpenseData = incomeExpense
                )
            }
        }
    }

    private val _reportFlow = MutableStateFlow(
        generateReportFor("March 2025", PeriodType.MONTHLY)
    )
    override val reportFlow: StateFlow<FinancialReport> = _reportFlow.asStateFlow()

    override suspend fun getReport(monthYear: String, period: PeriodType): Result<FinancialReport> {
        val rep = generateReportFor(monthYear, period)
        _reportFlow.value = rep
        return Result.success(rep)
    }

    override suspend fun switchPeriod(period: PeriodType): FinancialReport {
        val current = _reportFlow.value
        val updated = generateReportFor(current.monthYear, period)
        _reportFlow.value = updated
        return updated
    }
}
