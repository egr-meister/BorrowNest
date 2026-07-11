package com.borrownest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemHistoryEvent
import com.borrownest.app.model.ItemHistoryEventType
import com.borrownest.app.ui.components.EmptyState
import com.borrownest.app.ui.viewmodel.BorrowViewModel
import com.borrownest.app.util.DateUtils

private enum class HistoryFilter(val label: String) {
    All("All"), Given("Given"), Borrowed("Borrowed"), Returned("Returned")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: BorrowViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf(HistoryFilter.All) }

    val itemsById = uiState.data.items.associateBy { it.id }

    val events = uiState.data.historyEvents
        .sortedByDescending { it.createdAt }
        .filter { event ->
            val item = itemsById[event.itemId]
            when (filter) {
                HistoryFilter.All -> true
                HistoryFilter.Given -> item?.direction == ItemDirection.Given
                HistoryFilter.Borrowed -> item?.direction == ItemDirection.Borrowed
                HistoryFilter.Returned -> event.eventType == ItemHistoryEventType.MarkedReturned
            }
        }

    Scaffold(
        topBar = { TopAppBar(title = { Text("History") }) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HistoryFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { filter = f },
                        label = { Text(f.label) }
                    )
                }
            }
            if (events.isEmpty()) {
                EmptyState(
                    message = "No item history yet.",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    items(items = events, key = { it.id }) { event ->
                        HistoryRow(event)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(event: ItemHistoryEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Text(event.eventType.name, style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        Text(event.description, style = MaterialTheme.typography.bodyMedium)
        Text(
            "${DateUtils.displayDate(event.eventDate, event.eventDate)} · ${event.eventTime}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
