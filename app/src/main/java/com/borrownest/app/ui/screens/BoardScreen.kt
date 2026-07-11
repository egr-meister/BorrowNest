package com.borrownest.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borrownest.app.model.BoardFilter
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.ui.components.ArchiveTray
import com.borrownest.app.ui.components.ReminderBanner
import com.borrownest.app.ui.components.SingleLaneList
import com.borrownest.app.ui.components.SpineInfo
import com.borrownest.app.ui.components.TwoSidedBoard
import com.borrownest.app.ui.viewmodel.BorrowViewModel
import com.borrownest.app.util.DateUtils
import com.borrownest.app.util.FilterSortUtils
import com.borrownest.app.model.DerivedItemStatus
import com.borrownest.app.util.StatusUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    viewModel: BorrowViewModel,
    onAddItem: () -> Unit,
    onItemClick: (String) -> Unit,
    onOpenArchive: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenPersons: () -> Unit,
    onOpenReturned: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val soon = uiState.soonThresholdDays

    var boardFilter by remember(uiState.settings.defaultBoardFilter) {
        mutableStateOf(uiState.settings.defaultBoardFilter)
    }
    var dismissedReminderId by remember { mutableStateOf<String?>(null) }
    var singleDirection by remember { mutableStateOf(ItemDirection.Given) }

    val allItems = uiState.data.items
    val given = FilterSortUtils.sortActiveBoard(
        FilterSortUtils.activeForDirection(allItems, ItemDirection.Given), soon
    )
    val borrowed = FilterSortUtils.sortActiveBoard(
        FilterSortUtils.activeForDirection(allItems, ItemDirection.Borrowed), soon
    )

    val givenOverdue = given.count {
        StatusUtils.derive(it, soon) == DerivedItemStatus.Overdue
    }
    val borrowedOverdue = borrowed.count {
        StatusUtils.derive(it, soon) == DerivedItemStatus.Overdue
    }
    val dueToday = (given + borrowed).count {
        StatusUtils.derive(it, soon) == DerivedItemStatus.DueToday
    }

    val nearest = FilterSortUtils.nearestExpectedReturn(allItems)
    val spineInfo = SpineInfo(
        todayLabel = DateUtils.displayDateShort(DateUtils.format(DateUtils.today())),
        nearestReturnLabel = nearest?.let {
            "${it.itemName.take(14)} · ${DateUtils.displayDateShort(it.expectedReturnDate)}"
        } ?: "None scheduled",
        dueTodayCount = dueToday,
        overdueCount = givenOverdue + borrowedOverdue
    )

    val archivedCount = allItems.count { it.archived }
    val visibleReminder = reminders.firstOrNull { it.itemId != dismissedReminderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BorrowNest", fontWeight = FontWeight.Bold)
                        Text(
                            DateUtils.displayDate(DateUtils.format(DateUtils.today())),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleBoardListMode() }) {
                        Icon(
                            if (uiState.boardListMode) Icons.Filled.Dashboard
                            else Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = if (uiState.boardListMode) "Board view" else "List view"
                        )
                    }
                    IconButton(onClick = onOpenReturned) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = "Returned items")
                    }
                    IconButton(onClick = onOpenPersons) {
                        Icon(Icons.Filled.People, contentDescription = "People")
                    }
                    IconButton(onClick = onOpenStatistics) {
                        Icon(Icons.Filled.BarChart, contentDescription = "Statistics")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddItem,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add Item") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (visibleReminder != null) {
                ReminderBanner(
                    reminder = visibleReminder,
                    remainingCount = reminders.count { it.itemId != dismissedReminderId },
                    onOpenItem = onItemClick,
                    onDismiss = { dismissedReminderId = visibleReminder.itemId },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            if (uiState.boardListMode) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
                ) {
                    SegmentedButton(
                        selected = singleDirection == ItemDirection.Given,
                        onClick = { singleDirection = ItemDirection.Given },
                        shape = SegmentedButtonDefaults.itemShape(0, 2)
                    ) { Text("Given (${given.size})") }
                    SegmentedButton(
                        selected = singleDirection == ItemDirection.Borrowed,
                        onClick = { singleDirection = ItemDirection.Borrowed },
                        shape = SegmentedButtonDefaults.itemShape(1, 2)
                    ) { Text("Borrowed (${borrowed.size})") }
                }
                SingleLaneList(
                    items = if (singleDirection == ItemDirection.Given) given else borrowed,
                    direction = singleDirection,
                    soonThresholdDays = soon,
                    onItemClick = onItemClick,
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.weight(1f)
                )
            } else {
                val filteredGiven = if (boardFilter == BoardFilter.Borrowed) emptyList() else given
                val filteredBorrowed = if (boardFilter == BoardFilter.Given) emptyList() else borrowed
                BoardFilterRow(boardFilter) { boardFilter = it }
                TwoSidedBoard(
                    givenItems = filteredGiven,
                    borrowedItems = filteredBorrowed,
                    soonThresholdDays = soon,
                    spineInfo = spineInfo,
                    givenOverdue = givenOverdue,
                    borrowedOverdue = borrowedOverdue,
                    onItemClick = onItemClick,
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 96.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            ArchiveTray(
                archivedCount = archivedCount,
                onOpen = onOpenArchive,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoardFilterRow(current: BoardFilter, onChange: (BoardFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BoardFilter.entries.forEach { filter ->
            FilterChip(
                selected = current == filter,
                onClick = { onChange(filter) },
                label = { Text(filter.name) }
            )
        }
    }
}
