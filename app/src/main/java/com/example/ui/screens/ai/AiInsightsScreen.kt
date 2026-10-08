package com.example.ui.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AiChatMessageBubble
import com.example.ui.components.AiCoachCard
import com.example.ui.components.AiInsightCard
import com.example.ui.components.AiQuestionInput
import com.example.ui.components.AiSummaryCard
import com.example.ui.components.AnomalyInsightCard
import com.example.ui.components.AppHeader
import com.example.ui.components.SuggestedQuestionChip
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PurpleCoach
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AiInsightsUiState
import com.example.ui.viewmodel.AiInsightsViewModel

@Composable
fun AiInsightsScreen(
    viewModel: AiInsightsViewModel,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val questionInput by viewModel.questionInput.collectAsState()
    val reviewDialogVisible by viewModel.reviewDialogVisible.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("ai_insights_screen")
    ) {
        // App Header
        AppHeader(
            title = "AI Insights",
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            notificationCount = 2
        )

        when (val state = uiState) {
            is AiInsightsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("ai_loading_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TealPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing your spending insights...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is AiInsightsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .testTag("ai_error_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        IconButton(onClick = { viewModel.loadInsights() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                        }
                    }
                }
            }

            is AiInsightsUiState.Success -> {
                val insights = state.insights
                val summary = state.summary

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("ai_insights_scroll_content"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // AI Monthly Coach
                    item {
                        AiCoachCard(
                            title = insights.coachTitle,
                            headline = insights.coachSubtitle,
                            body = insights.coachDescription
                        )
                    }

                    // YOUR INSIGHTS Section Header
                    item {
                        Text(
                            text = "Your Insights",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // CARD 1: Food is your top category
                    item {
                        AiInsightCard(
                            title = insights.topCategory.insight,
                            metric = insights.topCategory.amountFormatted,
                            detail = "Accounting for ${insights.topCategory.percentage}% of your expenses",
                            icon = Icons.Default.Fastfood,
                            accentColor = TealPrimary
                        )
                    }

                    // CARD 2: Weekend spending is up
                    item {
                        AiInsightCard(
                            title = insights.trendAlert,
                            metric = insights.trendDetail,
                            detail = "Elevated dining and entertainment activity",
                            icon = Icons.Default.TrendingUp,
                            accentColor = WarningAmber
                        )
                    }

                    // CARD 3: You could save PKR 3,200
                    item {
                        AiInsightCard(
                            title = insights.savingSuggestion,
                            metric = insights.savingTarget,
                            detail = insights.savingDetail,
                            icon = Icons.Default.Savings,
                            accentColor = TealPrimary
                        )
                    }

                    // CARD 4: Anomaly card (only shown if isDetected == true)
                    item {
                        AnimatedVisibility(visible = insights.anomalyDetected.isDetected) {
                            AnomalyInsightCard(
                                title = insights.anomalyDetected.message,
                                detail = "${insights.anomalyDetected.category} — ${insights.anomalyDetected.amountFormatted} on ${insights.anomalyDetected.date}",
                                onReviewClick = { viewModel.onReviewClicked() }
                            )
                        }
                    }

                    // ASK AI Section
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Ask AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Suggested questions chips (horizontal scroll)
                            val scrollState = rememberScrollState()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(scrollState),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SuggestedQuestionChip(
                                    text = "Where am I spending most?",
                                    onClick = { viewModel.sendQuestion("Where am I spending most?") }
                                )
                                SuggestedQuestionChip(
                                    text = "How can I save more?",
                                    onClick = { viewModel.sendQuestion("How can I save more?") }
                                )
                                SuggestedQuestionChip(
                                    text = "Compare with February",
                                    onClick = { viewModel.sendQuestion("Compare with February") }
                                )
                                SuggestedQuestionChip(
                                    text = "Summarize my month",
                                    onClick = { viewModel.sendQuestion("Summarize my month") }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Question text input & send button
                            AiQuestionInput(
                                value = questionInput,
                                onValueChange = { viewModel.onQuestionInputChanged(it) },
                                onSend = { viewModel.sendQuestion() },
                                isLoading = isAiThinking
                            )
                        }
                    }

                    // Interactive AI Chat conversation bubbles
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_conversation_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = "Smart Financial Assistant",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    ),
                                    color = TealPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                chatMessages.forEach { msg ->
                                    AiChatMessageBubble(message = msg)
                                }
                            }
                        }
                    }

                    // MARCH SUMMARY Card
                    item {
                        AiSummaryCard(summary = summary)
                    }

                    item {
                        Spacer(modifier = Modifier.height(28.dp))
                    }
                }
            }
        }
    }

    // Anomaly Review Dialog
    if (reviewDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissReviewDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = WarningAmber
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Review Purchase Anomaly")
                }
            },
            text = {
                Text(
                    text = "A purchase of PKR 4,800 on Mar 9 in Shopping was flagged as unusually high compared to your typical PKR 1,200 shopping transactions.\n\nWould you like to approve this purchase as expected?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmAnomalyReview() },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                ) {
                    Text("Approve as Expected")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissReviewDialog() }) {
                    Text("Keep Alert")
                }
            }
        )
    }
}
