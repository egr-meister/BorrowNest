package com.borrownest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borrownest.app.model.BoardFilter
import com.borrownest.app.ui.components.ConfirmDialog
import com.borrownest.app.ui.components.Copy
import com.borrownest.app.ui.viewmodel.BorrowViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BorrowViewModel,
    onOpenStatistics: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settings = uiState.settings
    val reminder = settings.reminderSettings

    var dialog by remember { mutableStateOf<SettingsDialog?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SettingsSection("Return timing") {
                Text("\"Soon\" threshold", style = MaterialTheme.typography.bodyLarge)
                Text("Items due within this many days are marked Due soon.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 3, 7, 14).forEach { days ->
                        FilterChip(
                            selected = settings.soonThresholdDays == days,
                            onClick = { viewModel.setSoonThreshold(days) },
                            label = { Text("$days day${if (days == 1) "" else "s"}") }
                        )
                    }
                }
            }

            SettingsSection("Board") {
                Text("Default board filter", style = MaterialTheme.typography.bodyLarge)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BoardFilter.entries.forEach { f ->
                        FilterChip(
                            selected = settings.defaultBoardFilter == f,
                            onClick = { viewModel.setDefaultBoardFilter(f) },
                            label = { Text(f.name) }
                        )
                    }
                }
            }

            SettingsSection("In-app reminders") {
                Text(Copy.REMINDER_EXPLANATION, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                ToggleRow("Enable reminders", reminder.enabled) { v ->
                    viewModel.updateReminderSettings { it.copy(enabled = v) }
                }
                ToggleRow("Due soon", reminder.showDueSoon, enabled = reminder.enabled) { v ->
                    viewModel.updateReminderSettings { it.copy(showDueSoon = v) }
                }
                ToggleRow("Due today", reminder.showDueToday, enabled = reminder.enabled) { v ->
                    viewModel.updateReminderSettings { it.copy(showDueToday = v) }
                }
                ToggleRow("Overdue", reminder.showOverdue, enabled = reminder.enabled) { v ->
                    viewModel.updateReminderSettings { it.copy(showOverdue = v) }
                }
                ToggleRow("No return date", reminder.showNoReturnDate, enabled = reminder.enabled) { v ->
                    viewModel.updateReminderSettings { it.copy(showNoReturnDate = v) }
                }
            }

            SettingsSection("Data") {
                ActionRow("View statistics", onClick = onOpenStatistics)
                ActionRow("Show onboarding again") { viewModel.showOnboardingAgain() }
                ActionRow("Archive all returned items") { dialog = SettingsDialog.ArchiveAll }
                ActionRow("Clear archive", destructive = true) { dialog = SettingsDialog.ClearArchive }
                ActionRow("Delete all Given records", destructive = true) {
                    dialog = SettingsDialog.DeleteGiven
                }
                ActionRow("Delete all Borrowed records", destructive = true) {
                    dialog = SettingsDialog.DeleteBorrowed
                }
                ActionRow("Reset all local data", destructive = true) {
                    dialog = SettingsDialog.ResetAll
                }
            }

            SettingsSection("About BorrowNest") {
                Text("Manual tracking", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(Copy.MANUAL_TRACKING_DISCLAIMER, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text("Privacy", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(Copy.PRIVACY_NOTE, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(Copy.PRIVACY_NOTE_LONG, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Text("BorrowNest 1.0.0 · Offline, local-only.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    when (dialog) {
        SettingsDialog.ArchiveAll -> ConfirmDialog(
            title = "Archive all returned items?",
            message = "All returned records will be moved to the archive.",
            confirmLabel = "Archive all",
            onConfirm = { viewModel.archiveAllReturned(); dialog = null },
            onDismiss = { dialog = null }
        )
        SettingsDialog.ClearArchive -> ConfirmDialog(
            title = "Clear archive?",
            message = "This permanently removes every archived record. This action cannot be undone.",
            confirmLabel = "Clear archive", destructive = true,
            onConfirm = { viewModel.clearArchive(); dialog = null },
            onDismiss = { dialog = null }
        )
        SettingsDialog.DeleteGiven -> ConfirmDialog(
            title = "Delete all Given records?",
            message = "This permanently removes every Given record. This action cannot be undone.",
            confirmLabel = "Delete Given", destructive = true,
            onConfirm = { viewModel.deleteAllGiven(); dialog = null },
            onDismiss = { dialog = null }
        )
        SettingsDialog.DeleteBorrowed -> ConfirmDialog(
            title = "Delete all Borrowed records?",
            message = "This permanently removes every Borrowed record. This action cannot be undone.",
            confirmLabel = "Delete Borrowed", destructive = true,
            onConfirm = { viewModel.deleteAllBorrowed(); dialog = null },
            onDismiss = { dialog = null }
        )
        SettingsDialog.ResetAll -> ConfirmDialog(
            title = "Reset all local data?",
            message = "This will permanently remove every item, person label, date, status, " +
                "note, history event, archive record, and setting stored by BorrowNest on this device.",
            confirmLabel = "Reset everything", destructive = true,
            onConfirm = { viewModel.resetAllData(); dialog = null },
            onDismiss = { dialog = null }
        )
        null -> {}
    }
}

private enum class SettingsDialog { ArchiveAll, ClearArchive, DeleteGiven, DeleteBorrowed, ResetAll }

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(6.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) { content() }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant)
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled)
    }
}

@Composable
private fun ActionRow(label: String, destructive: Boolean = false, onClick: () -> Unit) {
    Text(
        label,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        color = if (destructive) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurface
    )
}
