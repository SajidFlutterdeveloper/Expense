package com.example.data.model

data class CategoryBudget(
    val categoryId: Int,
    val name: String,
    val limitAmount: Double,
    val spentAmount: Double,
    val iconKey: String = "category"
) {
    val percentage: Int
        get() = if (limitAmount > 0) ((spentAmount / limitAmount) * 100).toInt() else 0

    val remainingAmount: Double
        get() = (limitAmount - spentAmount).coerceAtLeast(0.0)
}

data class MonthlyBudget(
    val monthYear: String = "March 2025",
    val totalBudget: Double = 50000.0,
    val spentAmount: Double = 35750.0,
    val status: String = "Near your limit",
    val categories: List<CategoryBudget> = emptyList()
) {
    val remainingAmount: Double
        get() = (totalBudget - spentAmount).coerceAtLeast(0.0)

    val percentageSpent: Int
        get() = if (totalBudget > 0) ((spentAmount / totalBudget) * 100).toInt() else 0
}

data class CategoryBudgetUpdateRequest(
    val categoryId: Int,
    val limitAmount: Double
)

data class BudgetUpdateRequest(
    val monthYear: String,
    val totalBudget: Double,
    val categoryBudgets: List<CategoryBudgetUpdateRequest>
)
