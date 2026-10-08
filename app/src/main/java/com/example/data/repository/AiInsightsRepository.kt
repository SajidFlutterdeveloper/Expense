package com.example.data.repository

import com.example.data.model.AiChatMessage
import com.example.data.model.AiInsightData
import com.example.data.model.AnomalyInsight
import com.example.data.model.AskAiRequest
import com.example.data.model.AskAiResponse
import com.example.data.model.AskAiData
import com.example.data.model.ChatSender
import com.example.data.model.MarchSummary
import com.example.data.model.TopCategoryInsight
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface AiInsightsRepository {
    val insightsFlow: StateFlow<AiInsightData>
    val summaryFlow: StateFlow<MarchSummary>
    val chatMessagesFlow: StateFlow<List<AiChatMessage>>

    suspend fun fetchInsights(): Result<AiInsightData>
    suspend fun askAi(request: AskAiRequest): Result<AskAiResponse>
    suspend fun dismissAnomaly()
}

class AiInsightsRepositoryImpl : AiInsightsRepository {

    private val _insightsFlow = MutableStateFlow(
        AiInsightData(
            coachTitle = "Your March money coach",
            coachSubtitle = "Your finances are looking strong",
            coachDescription = "Here are the patterns and opportunities I found in your spending.",
            topCategory = TopCategoryInsight(
                name = "Food",
                insight = "Food is your top category",
                amountFormatted = "PKR 9,450 / 26%",
                percentage = 26
            ),
            trendAlert = "Weekend spending is up",
            trendDetail = "+18% vs last month",
            savingSuggestion = "You could save PKR 3,200",
            savingDetail = "Try a PKR 800 weekly dining limit",
            savingTarget = "PKR 3,200",
            anomalyDetected = AnomalyInsight(
                isDetected = true,
                message = "Higher-than-usual purchase detected",
                category = "Shopping",
                amountFormatted = "PKR 4,800",
                date = "Mar 9"
            )
        )
    )
    override val insightsFlow: StateFlow<AiInsightData> = _insightsFlow.asStateFlow()

    private val _summaryFlow = MutableStateFlow(
        MarchSummary(
            income = 120000.0,
            expenses = 35750.0,
            saved = 84250.0,
            status = "Excellent progress",
            updatedAt = "Updated today at 2:30 PM"
        )
    )
    override val summaryFlow: StateFlow<MarchSummary> = _summaryFlow.asStateFlow()

    private val _chatMessagesFlow = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = UUID.randomUUID().toString(),
                sender = ChatSender.AI,
                text = "Hello Sajid! I've analyzed your March finances. You've saved PKR 84,250 so far. How can I assist with your budget today?",
                timestamp = "2:15 PM"
            )
        )
    )
    override val chatMessagesFlow: StateFlow<List<AiChatMessage>> = _chatMessagesFlow.asStateFlow()

    override suspend fun fetchInsights(): Result<AiInsightData> {
        return Result.success(_insightsFlow.value)
    }

    override suspend fun askAi(request: AskAiRequest): Result<AskAiResponse> {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val userTime = timeFormat.format(Date())

        // Append user question
        val userMsg = AiChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.USER,
            text = request.question,
            timestamp = userTime
        )
        val tempThinkingMsg = AiChatMessage(
            id = "thinking-temp",
            sender = ChatSender.AI,
            text = "Analyzing your spending records...",
            timestamp = userTime,
            isThinking = true
        )

        _chatMessagesFlow.value = _chatMessagesFlow.value + userMsg + tempThinkingMsg

        // Simulate network processing
        delay(700)

        val qLower = request.question.lowercase(Locale.getDefault())
        val answerText = when {
            qLower.contains("where") && qLower.contains("spend") -> {
                "You are spending the most on Food (PKR 9,450, 26% of total expenses) followed by Bills at 87% limit utilization (PKR 8,700) and Shopping (PKR 6,240)."
            }
            qLower.contains("save more") || qLower.contains("saving") -> {
                "You can save PKR 3,200 this month by setting an PKR 800 weekly cap on dining out and reducing weekend shopping trips, which are up 18%."
            }
            qLower.contains("compare") || qLower.contains("february") -> {
                "Compared to February (PKR 38,900 spent), your overall March spending is down by 8.1%! However, your weekend dining expenses spiked by 18%."
            }
            qLower.contains("summarize") || qLower.contains("summary") -> {
                "March Summary:\n• Total Income: PKR 1,20,000\n• Total Expenses: PKR 35,750\n• Net Saved: PKR 84,250 (70.2% savings rate)\nStatus: Excellent progress!"
            }
            qLower.contains("bill") -> {
                "Notice: Your Bills category is at 87% (PKR 8,700 of PKR 10,000 limit). You have PKR 1,300 remaining for utility payments this month."
            }
            else -> {
                "Based on your March budget, your total spending is PKR 35,750 against your PKR 50,000 monthly limit (71.5% spent). You are on track to save PKR 84,250."
            }
        }

        val aiMsg = AiChatMessage(
            id = UUID.randomUUID().toString(),
            sender = ChatSender.AI,
            text = answerText,
            timestamp = timeFormat.format(Date())
        )

        // Replace thinking with real response
        _chatMessagesFlow.value = _chatMessagesFlow.value.filter { it.id != "thinking-temp" } + aiMsg

        return Result.success(AskAiResponse(success = true, data = AskAiData(answer = answerText)))
    }

    override suspend fun dismissAnomaly() {
        val current = _insightsFlow.value
        _insightsFlow.value = current.copy(
            anomalyDetected = current.anomalyDetected.copy(isDetected = false)
        )
    }
}
