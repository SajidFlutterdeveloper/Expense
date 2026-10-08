package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BudgetUpdateRequest
import com.example.data.model.CategoryBudgetUpdateRequest
import com.example.data.model.FinancialReport
import com.example.data.model.MonthlyBudget
import com.example.data.model.PeriodType
import com.example.data.repository.BudgetRepository
import com.example.data.repository.ReportsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BudgetUiState {
    data object Loading : BudgetUiState
    data class Success(val budget: MonthlyBudget) : BudgetUiState
    data object Empty : BudgetUiState
    data class Error(val message: String) : BudgetUiState
}

sealed interface ReportsUiState {
    data object Loading : ReportsUiState
    data class Success(val report: FinancialReport) : ReportsUiState
    data class Error(val message: String) : ReportsUiState
}

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val reportsRepository: ReportsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    private val _reportsState = MutableStateFlow<ReportsUiState>(ReportsUiState.Loading)
    val reportsState: StateFlow<ReportsUiState> = _reportsState.asStateFlow()

    private val _selectedMonth = MutableStateFlow("March 2025")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(PeriodType.MONTHLY)
    val selectedPeriod: StateFlow<PeriodType> = _selectedPeriod.asStateFlow()

    // For editing screen: editable category limits map
    private val _editCategoryLimits = MutableStateFlow<Map<Int, Double>>(emptyMap())
    val editCategoryLimits: StateFlow<Map<Int, Double>> = _editCategoryLimits.asStateFlow()

    private val _editSaveStatus = MutableStateFlow<String?>(null)
    val editSaveStatus: StateFlow<String?> = _editSaveStatus.asStateFlow()

    init {
        loadBudgetData()
        loadReportsData()
    }

    fun loadBudgetData() {
        viewModelScope.launch {
            _uiState.value = BudgetUiState.Loading
            delay(200) // smooth transition
            try {
                budgetRepository.monthlyBudgetFlow.collect { budget ->
                    if (budget.categories.isEmpty()) {
                        _uiState.value = BudgetUiState.Empty
                    } else {
                        _uiState.value = BudgetUiState.Success(budget)
                        // populate edit map if empty
                        if (_editCategoryLimits.value.isEmpty()) {
                            _editCategoryLimits.value = budget.categories.associate { it.categoryId to it.limitAmount }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = BudgetUiState.Error("Unable to load budget information: ${e.localizedMessage}")
            }
        }
    }

    fun loadReportsData() {
        viewModelScope.launch {
            _reportsState.value = ReportsUiState.Loading
            delay(150)
            try {
                reportsRepository.reportFlow.collect { report ->
                    _reportsState.value = ReportsUiState.Success(report)
                }
            } catch (e: Exception) {
                _reportsState.value = ReportsUiState.Error("Unable to load reports data.")
            }
        }
    }

    fun setPeriod(period: PeriodType) {
        _selectedPeriod.value = period
        viewModelScope.launch {
            reportsRepository.switchPeriod(period)
        }
    }

    fun changeMonth(newMonth: String) {
        _selectedMonth.value = newMonth
        viewModelScope.launch {
            budgetRepository.switchMonth(newMonth)
            reportsRepository.getReport(newMonth, _selectedPeriod.value)
        }
    }

    fun updateCategoryLimit(categoryId: Int, limit: Double) {
        val current = _editCategoryLimits.value.toMutableMap()
        current[categoryId] = limit.coerceAtLeast(0.0)
        _editCategoryLimits.value = current
    }

    fun saveBudget(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentBudget = if (currentState is BudgetUiState.Success) currentState.budget else return@launch

            val categoryRequests = _editCategoryLimits.value.map { (catId, limit) ->
                CategoryBudgetUpdateRequest(categoryId = catId, limitAmount = limit)
            }
            val totalLimit = _editCategoryLimits.value.values.sum()

            val request = BudgetUpdateRequest(
                monthYear = _selectedMonth.value,
                totalBudget = totalLimit,
                categoryBudgets = categoryRequests
            )

            val result = budgetRepository.updateBudget(request)
            if (result.isSuccess) {
                _editSaveStatus.value = "Budget saved successfully!"
                onSuccess()
            } else {
                _editSaveStatus.value = "Error saving budget. Please try again."
            }
        }
    }

    fun clearSaveStatus() {
        _editSaveStatus.value = null
    }
}
