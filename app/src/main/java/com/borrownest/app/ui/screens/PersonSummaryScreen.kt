package com.borrownest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borrownest.app.ui.components.EmptyState
import com.borrownest.app.ui.viewmodel.BorrowViewModel
import com.borrownest.app.util.PersonSummary
import com.borrownest.app.util.PersonUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonSummaryScreen(
    viewModel: BorrowViewModel,
    onBack: () -> Unit,
    onPersonClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summaries = PersonUtils.summarize(uiState.data.items, uiState.soonThresholdDays)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("People") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Grouped from names you entered manually. These are labels, not contacts, " +
                    "and similar names are never merged automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            if (summaries.isEmpty()) {
                EmptyState(message = "No people yet.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    items(items = summaries, key = { it.displayName.lowercase() }) { s ->
                        PersonRow(s) { onPersonClick(s.displayName) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonRow(summary: PersonSummary, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(summary.displayName.ifBlank { "Unnamed" },
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Metric("Given", summary.activeGiven)
            Metric("Borrowed", summary.activeBorrowed)
            Metric("Overdue", summary.overdue)
            Metric("Returned", summary.returnedCount)
        }
        if (summary.latestRecordDate.isNotBlank()) {
            Text("Latest record: ${summary.latestRecordDate}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Metric(label: String, value: Int) {
    Column(modifier = Modifier.weight(1f)) {
        Text("$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
