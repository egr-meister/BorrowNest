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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.ui.components.ConfirmDialog
import com.borrownest.app.ui.components.EmptyState
import com.borrownest.app.ui.viewmodel.BorrowViewModel
import com.borrownest.app.util.ArchiveFilter
import com.borrownest.app.util.ArchiveUtils
import com.borrownest.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    viewModel: BorrowViewModel,
    onItemClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf<ArchiveFilter>(ArchiveFilter.All) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }

    val archived = ArchiveUtils.apply(uiState.data.items, filter)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Archive") }) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(filter == ArchiveFilter.All, { filter = ArchiveFilter.All },
                    label = { Text("All") })
                FilterChip(filter == ArchiveFilter.Given, { filter = ArchiveFilter.Given },
                    label = { Text("Given") })
                FilterChip(filter == ArchiveFilter.Borrowed, { filter = ArchiveFilter.Borrowed },
                    label = { Text("Borrowed") })
                ArchiveUtils.archivedYears(uiState.data.items).forEach { year ->
                    FilterChip(
                        (filter as? ArchiveFilter.Year)?.year == year,
                        { filter = ArchiveFilter.Year(year) },
                        label = { Text(year.toString()) }
                    )
                }
            }

            if (archived.isEmpty()) {
                EmptyState(message = "No archived items yet.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    items(items = archived, key = { it.id }) { item ->
                        ArchiveRow(
                            item = item,
                            onOpen = { onItemClick(item.id) },
                            onRestore = { viewModel.restoreItem(item.id) },
                            onDelete = { pendingDelete = item.id }
                        )
                    }
                }
            }
        }
    }

    val deleteId = pendingDelete
    if (deleteId != null) {
        ConfirmDialog(
            title = "Delete this archived record permanently?",
            message = "This action cannot be undone.",
            confirmLabel = "Delete permanently",
            destructive = true,
            onConfirm = { viewModel.permanentlyDelete(deleteId); pendingDelete = null },
            onDismiss = { pendingDelete = null }
        )
    }
}

@Composable
private fun ArchiveRow(
    item: BorrowItem,
    onOpen: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
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
        Text("Returned: ${DateUtils.displayDateShort(item.actualReturnDate, "—")}",
            style = MaterialTheme.typography.labelSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onOpen) { Text("Open") }
            TextButton(onClick = onRestore) { Text("Restore") }
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
