package com.borrownest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borrownest.app.ui.theme.BorrowedIndigo
import com.borrownest.app.ui.theme.GivenTerracotta
import com.borrownest.app.ui.theme.SpineAccent
import com.borrownest.app.ui.theme.StatusReturned
import com.borrownest.app.ui.viewmodel.BorrowViewModel
import com.borrownest.app.util.StatsUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: BorrowViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stats = StatsUtils.compute(uiState.data.items, uiState.soonThresholdDays)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("Active items", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            GivenBorrowedBar(stats.activeGiven, stats.activeBorrowed)

            Spacer(Modifier.height(16.dp))
            NumericRow("Active given", stats.activeGiven)
            NumericRow("Active borrowed", stats.activeBorrowed)
            NumericRow("Overdue given", stats.overdueGiven)
            NumericRow("Overdue borrowed", stats.overdueBorrowed)
            NumericRow("Due today", stats.dueToday)
            NumericRow("No return date", stats.noReturnDate)
            NumericRow("Returned this month", stats.returnedThisMonth)
            NumericRow("Archived total", stats.archivedTotal)
            NumericRow("Most used category", 0, textValue = stats.mostUsedCategory)
            NumericRow(
                "Avg. active duration",
                0,
                textValue = stats.averageActiveDurationDays?.let { "$it days" } ?: "Not enough data"
            )

            Spacer(Modifier.height(20.dp))
            Text("Returned per month", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            MonthlyColumns(stats.monthlyReturned)

            Spacer(Modifier.height(16.dp))
            Text(
                "These are neutral personal summaries. BorrowNest does not rate people " +
                    "or generate reliability, trust, risk, or debt scores.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GivenBorrowedBar(given: Int, borrowed: Int) {
    val total = (given + borrowed).coerceAtLeast(1)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .weight(given.coerceAtLeast(0).toFloat().coerceAtLeast(0.001f) / total)
                .fillMaxHeight()
                .background(GivenTerracotta),
            contentAlignment = Alignment.Center
        ) { if (given > 0) Text("$given", color = androidx.compose.ui.graphics.Color.White) }
        Box(
            modifier = Modifier
                .weight(borrowed.coerceAtLeast(0).toFloat().coerceAtLeast(0.001f) / total)
                .fillMaxHeight()
                .background(BorrowedIndigo),
            contentAlignment = Alignment.Center
        ) { if (borrowed > 0) Text("$borrowed", color = androidx.compose.ui.graphics.Color.White) }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text("Given", style = MaterialTheme.typography.labelSmall, color = GivenTerracotta)
        Spacer(Modifier.weight(1f))
        Text("Borrowed", style = MaterialTheme.typography.labelSmall, color = BorrowedIndigo)
    }
}

@Composable
private fun NumericRow(label: String, value: Int, textValue: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(textValue ?: value.toString(), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MonthlyColumns(data: List<com.borrownest.app.util.MonthlyReturnedCount>) {
    val maxCount = (data.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { month ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${month.count}", style = MaterialTheme.typography.labelSmall)
                val fraction = (month.count.toFloat() / maxCount).coerceIn(0.02f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(if (month.count > 0) StatusReturned else SpineAccent.copy(alpha = 0.3f))
                )
                Text(month.label, style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center)
            }
        }
    }
}
