package com.example.data.repository

import com.example.data.model.BudgetUpdateRequest
import com.example.data.model.CategoryBudget
import com.example.data.model.MonthlyBudget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface BudgetRepository {
    val monthlyBudgetFlow: StateFlow<MonthlyBudget>
    suspend fun getBudget(monthYear: String): Result<MonthlyBudget>
    suspend fun updateBudget(request: BudgetUpdateRequest): Result<MonthlyBudget>
    suspend fun switchMonth(monthYear: String): MonthlyBudget
}

class BudgetRepositoryImpl : BudgetRepository {

    private val initialCategories = listOf(
        CategoryBudget(
            categoryId = 1,
            name = "Food",
            limitAmount = 12000.0,
            spentAmount = 9480.0,
            iconKey = "restaurant"
        ),
        CategoryBudget(
            categoryId = 2,
            name = "Transport",
            limitAmount = 6000.0,
            spentAmount = 4260.0,
            iconKey = "directions_car"
        ),
        CategoryBudget(
            categoryId = 3,
            name = "Shopping",
            limitAmount = 8000.0,
            spentAmount = 6240.0,
            iconKey = "shopping_bag"
        ),
        CategoryBudget(
            categoryId = 4,
            name = "Bills",
            limitAmount = 10000.0,
            spentAmount = 8700.0,
            iconKey = "receipt_long"
        ),
        CategoryBudget(
            categoryId = 5,
            name = "Health",
            limitAmount = 5000.0,
            spentAmount = 2100.0,
            iconKey = "medical_services"
        ),
        CategoryBudget(
            categoryId = 6,
            name = "Education",
            limitAmount = 4000.0,
            spentAmount = 2800.0,
            iconKey = "school"
        ),
        CategoryBudget(
            categoryId = 7,
            name = "Other",
            limitAmount = 5000.0,
            spentAmount = 2170.0,
            iconKey = "more_horiz"
        )
    )

    private val _monthlyBudgetFlow = MutableStateFlow(
        MonthlyBudget(
            monthYear = "March 2025",
            totalBudget = 50000.0,
            spentAmount = 35750.0,
            status = "Near your limit",
            categories = initialCategories
        )
    )
    override val monthlyBudgetFlow: StateFlow<MonthlyBudget> = _monthlyBudgetFlow.asStateFlow()

    override suspend fun getBudget(monthYear: String): Result<MonthlyBudget> {
        return Result.success(_monthlyBudgetFlow.value)
    }

    override suspend fun updateBudget(request: BudgetUpdateRequest): Result<MonthlyBudget> {
        val current = _monthlyBudgetFlow.value
        val updatedCategoryList = current.categories.map { existingCat ->
            val match = request.categoryBudgets.find { it.categoryId == existingCat.categoryId }
            if (match != null) {
                existingCat.copy(limitAmount = match.limitAmount)
            } else {
                existingCat
            }
        }

        val totalSpent = updatedCategoryList.sumOf { it.spentAmount }
        val percentage = if (request.totalBudget > 0) (totalSpent / request.totalBudget) * 100 else 0.0
        val newStatus = when {
            percentage >= 90.0 -> "Critical limit"
            percentage >= 70.0 -> "Near your limit"
            else -> "On track"
        }

        val updatedBudget = current.copy(
            monthYear = request.monthYear,
            totalBudget = request.totalBudget,
            spentAmount = totalSpent,
            status = newStatus,
            categories = updatedCategoryList
        )

        _monthlyBudgetFlow.value = updatedBudget
        return Result.success(updatedBudget)
    }

    override suspend fun switchMonth(monthYear: String): MonthlyBudget {
        val current = _monthlyBudgetFlow.value
        val updated = current.copy(monthYear = monthYear)
        _monthlyBudgetFlow.value = updated
        return updated
    }
}
