package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IncomeExpensePoint
import com.example.data.model.SpendingTrendPoint
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.TealPrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SpendingTrendChart(
    points: List<SpendingTrendPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxVal = (points.maxOfOrNull { it.amount } ?: 1.0) * 1.15

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("spending_trend_chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending Trend",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                selectedIndex?.let { idx ->
                    if (idx in points.indices) {
                        val pt = points[idx]
                        Text(
                            text = "${pt.label}: PKR ${NumberFormat.getNumberInstance(Locale.US).format(pt.amount.toLong())}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = TealPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val lineColor = TealPrimary
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val step = size.width / points.size
                            val index = (offset.x / step).toInt().coerceIn(0, points.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                val w = size.width
                val h = size.height - 30.dp.toPx()
                val topPadding = 10.dp.toPx()
                val usableHeight = h - topPadding

                // Grid lines (3 horizontal lines)
                for (i in 0..2) {
                    val y = topPadding + (usableHeight / 2) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val stepX = w / (points.size - 1).coerceAtLeast(1)
                val path = Path()
                val fillPath = Path()

                points.forEachIndexed { index, pt ->
                    val x = index * stepX
                    val y = topPadding + usableHeight - ((pt.amount / maxVal).toFloat() * usableHeight)

                    if (index == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, h)
                        fillPath.lineTo(x, y)
                    } else {
                        // Smooth cubic bezier
                        val prevX = (index - 1) * stepX
                        val prevPt = points[index - 1]
                        val prevY = topPadding + usableHeight - ((prevPt.amount / maxVal).toFloat() * usableHeight)
                        val cx1 = prevX + (x - prevX) / 2
                        val cx2 = prevX + (x - prevX) / 2
                        path.cubicTo(cx1, prevY, cx2, y, x, y)
                        fillPath.cubicTo(cx1, prevY, cx2, y, x, y)
                    }

                    if (index == points.size - 1) {
                        fillPath.lineTo(x, h)
                        fillPath.close()
                    }
                }

                // Gradient area under curve
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f)),
                        startY = topPadding,
                        endY = h
                    )
                )

                // Line curve
                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Points & text
                points.forEachIndexed { index, pt ->
                    val x = index * stepX
                    val y = topPadding + usableHeight - ((pt.amount / maxVal).toFloat() * usableHeight)
                    val isSelected = selectedIndex == index

                    drawCircle(
                        color = if (isSelected) lineColor else Color.White,
                        radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = lineColor,
                        radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                        center = Offset(x, y),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // X-axis labels below canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { pt ->
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun IncomeExpenseChart(
    data: List<IncomeExpensePoint>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxVal = (data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 1.0) * 1.15

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("income_expense_chart"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Income vs. expense",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val w = size.width
                val h = size.height - 24.dp.toPx()
                val groupWidth = w / data.size
                val barWidth = (groupWidth * 0.28f).coerceAtMost(22.dp.toPx())

                // Grid lines
                for (i in 0..2) {
                    val y = (h / 2) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                data.forEachIndexed { index, pt ->
                    val centerX = index * groupWidth + (groupWidth / 2)

                    // Income bar
                    val incomeHeight = ((pt.income / maxVal).toFloat() * h).coerceAtLeast(4f)
                    val incomeTop = h - incomeHeight
                    val incomeX = centerX - barWidth - 2.dp.toPx()

                    drawRoundRect(
                        color = IncomeGreen,
                        topLeft = Offset(incomeX, incomeTop),
                        size = Size(barWidth, incomeHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Expense bar
                    val expenseHeight = ((pt.expense / maxVal).toFloat() * h).coerceAtLeast(4f)
                    val expenseTop = h - expenseHeight
                    val expenseX = centerX + 2.dp.toPx()

                    drawRoundRect(
                        color = ExpenseRed,
                        topLeft = Offset(expenseX, expenseTop),
                        size = Size(barWidth, expenseHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }

            // Labels below bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                data.forEach { pt ->
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
