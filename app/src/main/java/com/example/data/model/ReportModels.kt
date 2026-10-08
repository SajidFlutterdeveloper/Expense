package com.example.data.model

enum class PeriodType(val label: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly")
}

data class SpendingTrendPoint(
    val label: String,
    val amount: Double
)

data class IncomeExpensePoint(
    val label: String,
    val income: Double,
    val expense: Double
)

data class FinancialReport(
    val monthYear: String = "March 2025",
    val selectedPeriod: PeriodType = PeriodType.MONTHLY,
    val totalIncome: Double = 120000.0,
    val totalExpenses: Double = 35750.0,
    val netSavings: Double = 84250.0,
    val highestCategory: String = "Shopping",
    val highestCategoryAmount: Double = 8250.0,
    val highestCategoryPercentage: Int = 24,
    val spendingTrendData: List<SpendingTrendPoint> = emptyList(),
    val incomeExpenseData: List<IncomeExpensePoint> = emptyList()
)
