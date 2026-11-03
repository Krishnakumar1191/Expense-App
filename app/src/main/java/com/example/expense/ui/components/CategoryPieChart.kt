package com.example.expense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expense.domain.model.ExpenseCategory
import java.text.NumberFormat
import java.util.Locale

val CategoryColors = mapOf(
    ExpenseCategory.FOOD to Color(0xFFFF9800),
    ExpenseCategory.SHOPPING to Color(0xFF9C27B0),
    ExpenseCategory.TRANSPORT to Color(0xFF2196F3),
    ExpenseCategory.RENT to Color(0xFF4CAF50),
    ExpenseCategory.BILLS to Color(0xFFF44336),
    ExpenseCategory.ENTERTAINMENT to Color(0xFFE91E63),
    ExpenseCategory.OTHER to Color(0xFF607D8B)
)

@Composable
fun CategoryPieChart(
    categoryTotals: Map<ExpenseCategory, Double>,
    modifier: Modifier = Modifier
) {
    val totalAmount = categoryTotals.values.sum()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    if (totalAmount <= 0.0) {
        Box(
            modifier = modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No spending data for this period", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        return
    }

    Row(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                var startAngle = -90f
                val strokeWidth = 32.dp.toPx()

                for ((category, amount) in categoryTotals) {
                    if (amount <= 0.0) continue
                    val sweepAngle = ((amount / totalAmount) * 360f).toFloat()
                    val color = CategoryColors[category] ?: Color.Gray

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total", fontSize = 11.sp, color = Color.Gray)
                Text(
                    currencyFormat.format(totalAmount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Column(
            modifier = Modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for ((category, amount) in categoryTotals) {
                if (amount <= 0.0) continue
                val color = CategoryColors[category] ?: Color.Gray
                val percentage = (amount / totalAmount * 100).toInt()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = color)
                    }
                    Text(
                        text = "${category.displayName}: ${currencyFormat.format(amount)} ($percentage%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
