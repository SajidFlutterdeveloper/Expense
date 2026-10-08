package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.repository.AiInsightsRepositoryImpl
import com.example.data.repository.BudgetRepositoryImpl
import com.example.data.repository.ReportsRepositoryImpl
import com.example.ui.components.AppBottomNavigation
import com.example.ui.components.AppNavDestination
import com.example.ui.screens.ai.AiInsightsScreen
import com.example.ui.screens.budget.BudgetScreen
import com.example.ui.screens.budget.EditBudgetScreen
import com.example.ui.screens.other.DashboardPlaceholderScreen
import com.example.ui.screens.other.TransactionsPlaceholderScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AiInsightsViewModel
import com.example.ui.viewmodel.BudgetViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val budgetRepository = BudgetRepositoryImpl()
        val reportsRepository = ReportsRepositoryImpl()
        val aiRepository = AiInsightsRepositoryImpl()

        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val budgetViewModel = remember {
                    BudgetViewModel(budgetRepository, reportsRepository)
                }
                val aiInsightsViewModel = remember {
                    AiInsightsViewModel(aiRepository)
                }

                SmartExpenseApp(
                    budgetViewModel = budgetViewModel,
                    aiInsightsViewModel = aiInsightsViewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun SmartExpenseApp(
    budgetViewModel: BudgetViewModel,
    aiInsightsViewModel: AiInsightsViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppNavDestination.BUDGET) }
    var isEditingBudget by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!isEditingBudget) {
                AppBottomNavigation(
                    currentDestination = currentDestination,
                    onDestinationSelected = { destination ->
                        currentDestination = destination
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isEditingBudget) {
                EditBudgetScreen(
                    viewModel = budgetViewModel,
                    onNavigateBack = { isEditingBudget = false },
                    onToggleTheme = onToggleTheme,
                    isDarkTheme = isDarkTheme
                )
            } else {
                when (currentDestination) {
                    AppNavDestination.BUDGET -> {
                        BudgetScreen(
                            viewModel = budgetViewModel,
                            onNavigateToEditBudget = { isEditingBudget = true },
                            onToggleTheme = onToggleTheme,
                            isDarkTheme = isDarkTheme
                        )
                    }

                    AppNavDestination.AI_INSIGHTS -> {
                        AiInsightsScreen(
                            viewModel = aiInsightsViewModel,
                            onToggleTheme = onToggleTheme,
                            isDarkTheme = isDarkTheme
                        )
                    }

                    AppNavDestination.DASHBOARD -> {
                        DashboardPlaceholderScreen(
                            onNavigateToBudget = { currentDestination = AppNavDestination.BUDGET },
                            onToggleTheme = onToggleTheme,
                            isDarkTheme = isDarkTheme
                        )
                    }

                    AppNavDestination.TRANSACTIONS -> {
                        TransactionsPlaceholderScreen(
                            onNavigateToBudget = { currentDestination = AppNavDestination.BUDGET },
                            onToggleTheme = onToggleTheme,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        }
    }
}
