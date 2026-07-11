package com.borrownest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.ui.theme.GivenDeep
import com.borrownest.app.ui.theme.GivenSoft
import com.borrownest.app.ui.theme.BorrowedDeep
import com.borrownest.app.ui.theme.BorrowedSoft

/**
 * The default two-sided board. One vertically scrolling list where each row
 * holds an optional Given tile (left) and an optional Borrowed tile (right),
 * separated by the central spine. Empty positions stay visually calm.
 */
@Composable
fun TwoSidedBoard(
    givenItems: List<BorrowItem>,
    borrowedItems: List<BorrowItem>,
    soonThresholdDays: Int,
    spineInfo: SpineInfo,
    givenOverdue: Int,
    borrowedOverdue: Int,
    onItemClick: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val rowCount = maxOf(givenItems.size, borrowedItems.size)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding
    ) {
        item(key = "board-header") {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                LaneHeader(
                    title = "Given",
                    subtitle = "${givenItems.size} active",
                    overdue = givenOverdue,
                    color = GivenDeep,
                    soft = GivenSoft,
                    modifier = Modifier.weight(1f)
                )
                Box(Modifier.width(112.dp).padding(horizontal = 4.dp)) {
                    ReturnSpineHeader(spineInfo)
                }
                LaneHeader(
                    title = "Borrowed",
                    subtitle = "${borrowedItems.size} active",
                    overdue = borrowedOverdue,
                    color = BorrowedDeep,
                    soft = BorrowedSoft,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (rowCount == 0) {
            item(key = "board-empty") {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                    Box(Modifier.weight(1f)) {
                        EmptyState(message = "No items given.")
                    }
                    Box(Modifier.width(112.dp), contentAlignment = Alignment.Center) {
                        VerticalSpine(Modifier.height(48.dp))
                    }
                    Box(Modifier.weight(1f)) {
                        EmptyState(message = "No items borrowed.")
                    }
                }
                EmptyState(
                    message = "Add an item to begin.",
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        } else {
            items(count = rowCount, key = { index -> "row-$index" }) { index ->
                val given = givenItems.getOrNull(index)
                val borrowed = borrowedItems.getOrNull(index)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(Modifier.weight(1f)) {
                        if (given != null) {
                            ItemTile(
                                item = given,
                                soonThresholdDays = soonThresholdDays,
                                onClick = { onItemClick(given.id) }
                            )
                        }
                    }
                    Box(
                        Modifier.width(112.dp).padding(horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        VerticalSpine(Modifier.height(64.dp))
                    }
                    Box(Modifier.weight(1f)) {
                        if (borrowed != null) {
                            ItemTile(
                                item = borrowed,
                                soonThresholdDays = soonThresholdDays,
                                onClick = { onItemClick(borrowed.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LaneHeader(
    title: String,
    subtitle: String,
    overdue: Int,
    color: androidx.compose.ui.graphics.Color,
    soft: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(soft.copy(alpha = 0.6f))
            .padding(10.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold, color = color)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (overdue > 0) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(com.borrownest.app.ui.theme.StatusOverdue)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "$overdue overdue",
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** Segmented, accessibility-friendly single-direction list. */
@Composable
fun SingleLaneList(
    items: List<BorrowItem>,
    direction: ItemDirection,
    soonThresholdDays: Int,
    onItemClick: (String) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        val msg = if (direction == ItemDirection.Given) "No items given." else "No items borrowed."
        EmptyState(message = msg, secondary = "Add an item to begin.", modifier = modifier)
        return
    }
    LazyColumn(modifier = modifier.fillMaxWidth(), contentPadding = contentPadding) {
        items(items = items, key = { it.id }) { item ->
            ItemTile(
                item = item,
                soonThresholdDays = soonThresholdDays,
                onClick = { onItemClick(item.id) },
                modifier = Modifier.padding(vertical = 5.dp)
            )
        }
    }
}
