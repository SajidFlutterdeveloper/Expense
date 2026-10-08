package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiChatMessage
import com.example.data.model.AiInsightData
import com.example.data.model.AskAiRequest
import com.example.data.model.MarchSummary
import com.example.data.repository.AiInsightsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AiInsightsUiState {
    data object Loading : AiInsightsUiState
    data class Success(
        val insights: AiInsightData,
        val summary: MarchSummary
    ) : AiInsightsUiState
    data class Error(val message: String) : AiInsightsUiState
}

class AiInsightsViewModel(
    private val aiRepository: AiInsightsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiInsightsUiState>(AiInsightsUiState.Loading)
    val uiState: StateFlow<AiInsightsUiState> = _uiState.asStateFlow()

    val chatMessages: StateFlow<List<AiChatMessage>> = aiRepository.chatMessagesFlow

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _questionInput = MutableStateFlow("")
    val questionInput: StateFlow<String> = _questionInput.asStateFlow()

    private val _reviewDialogVisible = MutableStateFlow(false)
    val reviewDialogVisible: StateFlow<Boolean> = _reviewDialogVisible.asStateFlow()

    init {
        loadInsights()
    }

    fun onQuestionInputChanged(newText: String) {
        _questionInput.value = newText
    }

    fun loadInsights() {
        viewModelScope.launch {
            _uiState.value = AiInsightsUiState.Loading
            delay(200)
            try {
                aiRepository.insightsFlow.collect { insights ->
                    val summary = aiRepository.summaryFlow.value
                    _uiState.value = AiInsightsUiState.Success(insights, summary)
                }
            } catch (e: Exception) {
                _uiState.value = AiInsightsUiState.Error("Unable to get AI insights. Please try again.")
            }
        }
    }

    fun sendQuestion(question: String? = null) {
        val qToSend = question?.trim() ?: _questionInput.value.trim()
        if (qToSend.isEmpty()) return

        _questionInput.value = ""
        viewModelScope.launch {
            _isAiThinking.value = true
            aiRepository.askAi(AskAiRequest(question = qToSend))
            _isAiThinking.value = false
        }
    }

    fun onReviewClicked() {
        _reviewDialogVisible.value = true
    }

    fun dismissReviewDialog() {
        _reviewDialogVisible.value = false
    }

    fun confirmAnomalyReview() {
        viewModelScope.launch {
            aiRepository.dismissAnomaly()
            _reviewDialogVisible.value = false
        }
    }
}
