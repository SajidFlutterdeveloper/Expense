package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.WarningAmber

@Composable
fun BudgetProgressBar(
    percentage: Int,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    customColor: Color? = null,
    trackColor: Color? = null
) {
    val progressRatio = (percentage.coerceIn(0, 100) / 100f)
    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = tween(durationMillis = 600),
        label = "budget_progress"
    )

    val progressColor = customColor ?: when {
        percentage >= 90 -> ExpenseRed
        percentage >= 75 -> WarningAmber
        else -> TealPrimary
    }

    val finalTrackColor = trackColor ?: MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(finalTrackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(height / 2))
                .background(progressColor)
        )
    }
}
