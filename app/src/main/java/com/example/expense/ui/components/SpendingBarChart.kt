package com.example.expense.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SpendingBarChart(
    personalTotal: Double,
    groupTotal: Double,
    modifier: Modifier = Modifier
) {
    val total = personalTotal + groupTotal
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Personal vs Group Spending", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            if (total <= 0) {
                Text("No spending recorded yet", fontSize = 12.sp, color = Color.Gray)
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Personal", fontSize = 12.sp, color = Color.Gray)
                        Text(currencyFormat.format(personalTotal), fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Group", fontSize = 12.sp, color = Color.Gray)
                        Text(currencyFormat.format(groupTotal), fontWeight = FontWeight.Bold, color = Color(0xFF43A047))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                ) {
                    val personalWidth = ((personalTotal / total) * size.width).toFloat()
                    val groupWidth = size.width - personalWidth

                    if (personalTotal > 0) {
                        drawRoundRect(
                            color = Color(0xFF1E88E5),
                            size = Size(personalWidth, size.height),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }

                    if (groupTotal > 0) {
                        drawRoundRect(
                            color = Color(0xFF43A047),
                            topLeft = Offset(personalWidth, 0f),
                            size = Size(groupWidth, size.height),
                            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
