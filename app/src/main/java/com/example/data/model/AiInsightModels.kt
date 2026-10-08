package com.example.data.model

data class TopCategoryInsight(
    val name: String,
    val insight: String,
    val amountFormatted: String,
    val percentage: Int
)

data class AnomalyInsight(
    val isDetected: Boolean,
    val message: String,
    val category: String,
    val amountFormatted: String,
    val date: String
)

data class AiInsightData(
    val coachTitle: String = "Your March money coach",
    val coachSubtitle: String = "Your finances are looking strong",
    val coachDescription: String = "Here are the patterns and opportunities I found in your spending.",
    val topCategory: TopCategoryInsight,
    val trendAlert: String,
    val trendDetail: String = "+18% vs last month",
    val savingSuggestion: String,
    val savingDetail: String = "Try a PKR 800 weekly dining limit",
    val savingTarget: String = "PKR 3,200",
    val anomalyDetected: AnomalyInsight
)

data class MarchSummary(
    val income: Double = 120000.0,
    val expenses: Double = 35750.0,
    val saved: Double = 84250.0,
    val status: String = "Excellent progress",
    val updatedAt: String = "Updated today at 2:30 PM"
)

enum class ChatSender {
    USER, AI
}

data class AiChatMessage(
    val id: String,
    val sender: ChatSender,
    val text: String,
    val timestamp: String,
    val isThinking: Boolean = false
)

data class AskAiRequest(
    val question: String
)

data class AskAiResponse(
    val success: Boolean,
    val data: AskAiData
)

data class AskAiData(
    val answer: String
)
