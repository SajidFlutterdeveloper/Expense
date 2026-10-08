package com.example

import com.example.data.model.BudgetUpdateRequest
import com.example.data.model.CategoryBudgetUpdateRequest
import com.example.data.repository.AiInsightsRepositoryImpl
import com.example.data.repository.BudgetRepositoryImpl
import com.example.data.repository.ReportsRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testInitialBudgetCalculation() = runBlocking {
        val repo = BudgetRepositoryImpl()
        val budget = repo.monthlyBudgetFlow.value

        assertEquals("March 2025", budget.monthYear)
        assertEquals(50000.0, budget.totalBudget, 0.01)
        assertEquals(35750.0, budget.spentAmount, 0.01)
        assertEquals(14250.0, budget.remainingAmount, 0.01)
        assertEquals(71, budget.percentageSpent)
        assertEquals("Near your limit", budget.status)
        assertTrue(budget.categories.isNotEmpty())

        val food = budget.categories.first { it.name == "Food" }
        assertEquals(79, food.percentage)

        val bills = budget.categories.first { it.name == "Bills" }
        assertEquals(87, bills.percentage)
    }

    @Test
    fun testBudgetUpdateContract() = runBlocking {
        val repo = BudgetRepositoryImpl()
        val updateReq = BudgetUpdateRequest(
            monthYear = "March 2025",
            totalBudget = 55000.0,
            categoryBudgets = listOf(
                CategoryBudgetUpdateRequest(categoryId = 1, limitAmount = 14000.0),
                CategoryBudgetUpdateRequest(categoryId = 4, limitAmount = 12000.0)
            )
        )

        val result = repo.updateBudget(updateReq)
        assertTrue(result.isSuccess)
        val updated = result.getOrThrow()

        val updatedFood = updated.categories.first { it.categoryId == 1 }
        assertEquals(14000.0, updatedFood.limitAmount, 0.01)
    }

    @Test
    fun testAiInsightsAndAskContract() = runBlocking {
        val aiRepo = AiInsightsRepositoryImpl()
        val insights = aiRepo.insightsFlow.value

        assertEquals("Food", insights.topCategory.name)
        assertTrue(insights.anomalyDetected.isDetected)

        val askRes = aiRepo.askAi(com.example.data.model.AskAiRequest("Where am I spending most?"))
        assertTrue(askRes.isSuccess)
        assertNotNull(askRes.getOrThrow().data.answer)
        assertTrue(askRes.getOrThrow().data.answer.contains("Food"))
    }
}
