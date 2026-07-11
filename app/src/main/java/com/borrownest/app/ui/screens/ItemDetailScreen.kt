package com.borrownest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.ui.components.ConfirmDialog
import com.borrownest.app.ui.components.ConfirmDialogWithContent
import com.borrownest.app.ui.components.Copy
import com.borrownest.app.ui.components.DateField
import com.borrownest.app.ui.components.StatusChip
import com.borrownest.app.ui.viewmodel.BorrowViewModel
import com.borrownest.app.util.DateUtils
import com.borrownest.app.util.StatusUtils

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    viewModel: BorrowViewModel,
    itemId: String?,
    onBack: () -> Unit,
    onEdit: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item = uiState.data.items.firstOrNull { it.id == itemId }

    if (item == null) {
        MissingItemFallback(onBack)
        return
    }

    val status = StatusUtils.derive(item, uiState.soonThresholdDays)
    val isReturned = item.lifecycleState == ItemLifecycleState.Returned

    var showReturnSheet by remember { mutableStateOf(false) }
    var showUndo by remember { mutableStateOf(false) }
    var showArchive by remember { mutableStateOf(false) }
    var showRestore by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.direction.label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(item.id) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
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
            Text(item.itemName.ifBlank { "Untitled item" },
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            StatusChip(status = status)

            Spacer(Modifier.height(16.dp))
            DetailRow("Direction", item.direction.label)
            DetailRow("Category", item.categoryLabel)
            DetailRow(
                if (item.direction == ItemDirection.Given) "Given to" else "Borrowed from",
                item.personName.ifBlank { "—" }
            )
            DetailRow("Record date", DateUtils.displayDate(item.recordDate))
            DetailRow(
                "Expected return",
                if (item.expectedReturnDate.isBlank()) "No return date"
                else DateUtils.displayDate(item.expectedReturnDate, "Return date unavailable")
            )
            if (isReturned) {
                DetailRow("Actual return", DateUtils.displayDate(item.actualReturnDate))
            }
            DetailRow("Priority", item.priority.name)
            if (item.note.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Note", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text(item.note, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(8.dp))
            DetailRow("Created", DateUtils.displayDate(item.createdAt.take(10), item.createdAt.take(10)))
            DetailRow("Updated", DateUtils.displayDate(item.updatedAt.take(10), item.updatedAt.take(10)))

            Spacer(Modifier.height(16.dp))
            Text(Copy.MANUAL_TRACKING_DISCLAIMER,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(20.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isReturned) {
                    Button(onClick = { showReturnSheet = true }) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null,
                            modifier = Modifier.height(18.dp))
                        Spacer(Modifier.height(0.dp))
                        Text(
                            if (item.direction == ItemDirection.Given) "  Mark received back"
                            else "  Mark returned"
                        )
                    }
                } else {
                    OutlinedButton(onClick = { showUndo = true }) {
                        Icon(Icons.Filled.Undo, contentDescription = null,
                            modifier = Modifier.height(18.dp))
                        Text("  Undo return")
                    }
                }
                if (item.archived) {
                    OutlinedButton(onClick = { showRestore = true }) {
                        Icon(Icons.Filled.Unarchive, contentDescription = null,
                            modifier = Modifier.height(18.dp))
                        Text("  Restore")
                    }
                } else if (isReturned) {
                    OutlinedButton(onClick = { showArchive = true }) {
                        Icon(Icons.Filled.Archive, contentDescription = null,
                            modifier = Modifier.height(18.dp))
                        Text("  Archive")
                    }
                }
                OutlinedButton(onClick = { showDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = null,
                        modifier = Modifier.height(18.dp))
                    Text("  Delete")
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showReturnSheet) {
        MarkReturnedDialog(
            direction = item.direction,
            onConfirm = { date, note ->
                viewModel.markReturned(item.id, date, note)
                showReturnSheet = false
            },
            onDismiss = { showReturnSheet = false }
        )
    }
    if (showUndo) {
        ConfirmDialog(
            title = "Move back to active?",
            message = "Move this item back to active? ${Copy.SAVED_MANUALLY_NOTE}",
            confirmLabel = "Move to active",
            onConfirm = { viewModel.undoReturn(item.id); showUndo = false },
            onDismiss = { showUndo = false }
        )
    }
    if (showArchive) {
        ConfirmDialog(
            title = "Archive record?",
            message = "Move this returned item to the archive?",
            confirmLabel = "Archive",
            onConfirm = { viewModel.archiveItem(item.id); showArchive = false },
            onDismiss = { showArchive = false }
        )
    }
    if (showRestore) {
        ConfirmDialog(
            title = "Restore record?",
            message = "Restore this item from the archive?",
            confirmLabel = "Restore",
            onConfirm = { viewModel.restoreItem(item.id); showRestore = false },
            onDismiss = { showRestore = false }
        )
    }
    if (showDelete) {
        ConfirmDialog(
            title = "Delete this record?",
            message = "This action cannot be undone.",
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.deleteItem(item.id)
                showDelete = false
                onBack()
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkReturnedDialog(
    direction: ItemDirection,
    onConfirm: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var date by remember { mutableStateOf(DateUtils.format(DateUtils.today())) }
    var note by remember { mutableStateOf("") }

    ConfirmDialogWithContent(
        title = if (direction == ItemDirection.Given) "Mark this item as received back?"
        else "Mark this item as returned?",
        confirmLabel = "Confirm",
        onConfirm = { onConfirm(date, note.ifBlank { null }) },
        onDismiss = onDismiss
    ) {
        Column {
            DateField(
                label = "Actual return date",
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 1000) note = it },
                label = { Text("Final note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(Copy.SAVED_MANUALLY_NOTE,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MissingItemFallback(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Item not found", style = MaterialTheme.typography.headlineSmall)
        Text("This record may have been deleted.",
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Back") }
    }
}
