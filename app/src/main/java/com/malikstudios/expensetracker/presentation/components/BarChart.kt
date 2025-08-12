package com.malikstudios.expensetracker.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BarChart(
    data: Map<LocalDate, Double>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    if (data.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                color = onSurface.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }
        return
    }

    val maxValue = data.values.maxOrNull() ?: 1.0
    val sortedData = data.toList().sortedBy { it.first }

    Column(modifier = modifier) {
        // Chart
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            val barWidth = size.width / sortedData.size * 0.7f
            val spacing = size.width / sortedData.size * 0.3f

            sortedData.forEachIndexed { index, (date, amount) ->
                val barHeight = (amount / maxValue) * size.height * 0.8f
                val x = index * (barWidth + spacing) + spacing / 2
                val y = size.height - barHeight

                drawRect(
                    color = primaryColor,
                    topLeft = Offset(x, y.toFloat()),
                    size = Size(barWidth, barHeight.toFloat())
                )
            }
        }

        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            sortedData.forEach { (date, _) ->
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MMM dd")),
                    fontSize = 10.sp,
                    color = onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}