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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.ui.components.EmptyState
import com.borrownest.app.ui.viewmodel.BorrowViewModel
import com.borrownest.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnedItemsScreen(
    viewModel: BorrowViewModel,
    onBack: () -> Unit,
    onItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val returned = uiState.data.items
        .filter { it.lifecycleState == ItemLifecycleState.Returned }
        .sortedByDescending { it.actualReturnDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Returned items") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (returned.isEmpty()) {
            EmptyState(
                message = "No returned items yet.",
                secondary = "Items you mark returned appear here before archiving.",
                modifier = Modifier.fillMaxSize().padding(padding)
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
        ) {
            items(items = returned, key = { it.id }) { item ->
                ReturnedRow(
                    item = item,
                    onClick = { onItemClick(item.id) },
                    onArchive = { viewModel.archiveItem(item.id) },
                    onUndo = { viewModel.undoReturn(item.id) }
                )
            }
        }
    }
}

@Composable
private fun ReturnedRow(
    item: BorrowItem,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onUndo: () -> Unit
) {
    // Neutral, factual timeliness label. No person scoring.
    val timeliness = run {
        val expected = DateUtils.parse(item.expectedReturnDate)
        val actual = DateUtils.parse(item.actualReturnDate)
        when {
            expected == null || actual == null -> "Returned"
            actual.isAfter(expected) -> "Returned after expected date"
            else -> "Returned on time"
        }
    }
    val personLabel = if (item.direction == ItemDirection.Given)
        "Given to ${item.personName}" else "Borrowed from ${item.personName}"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Text(item.itemName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(personLabel, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            "Expected: ${DateUtils.displayDateShort(item.expectedReturnDate, "—")} · " +
                "Returned: ${DateUtils.displayDateShort(item.actualReturnDate, "—")}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(timeliness, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onClick) { Text("Open") }
            if (!item.archived) {
                TextButton(onClick = onArchive) { Text("Archive") }
            }
            TextButton(onClick = onUndo) { Text("Undo return") }
        }
    }
}
