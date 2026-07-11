package com.borrownest.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.DerivedItemStatus
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemPriority
import com.borrownest.app.ui.theme.directionAccent
import com.borrownest.app.ui.theme.directionSoft
import com.borrownest.app.util.DateUtils
import com.borrownest.app.util.StatusUtils

/**
 * A single board tile. Given tiles are left-aligned with an outward arrow and
 * warm accent; Borrowed tiles are right-aligned with an inward arrow and cool
 * accent.
 */
@Composable
fun ItemTile(
    item: BorrowItem,
    soonThresholdDays: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val direction = item.direction
    val accent = directionAccent(direction)
    val soft = directionSoft(direction)
    val status = StatusUtils.derive(item, soonThresholdDays)

    val personLabel = when (direction) {
        ItemDirection.Given -> "Given to ${item.personName}"
        ItemDirection.Borrowed -> "Borrowed from ${item.personName}"
    }
    val returnLabel = when {
        item.expectedReturnDate.isBlank() -> "No return date"
        DateUtils.parse(item.expectedReturnDate) == null -> "Return date unavailable"
        direction == ItemDirection.Given ->
            "Expected back: ${DateUtils.displayDateShort(item.expectedReturnDate)}"
        else -> "Return by: ${DateUtils.displayDateShort(item.expectedReturnDate)}"
    }

    val description = "$personLabel. ${item.itemName}. $returnLabel. Status ${status.label}." +
        if (item.priority == ItemPriority.High) " High priority." else ""

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(soft.copy(alpha = 0.55f))
            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (direction == ItemDirection.Given) {
                    DirectionArrow(direction, accent)
                    CategoryGlyph(item, accent)
                    ItemName(item.itemName, Modifier.padding(start = 6.dp))
                } else {
                    ItemName(item.itemName, Modifier.weight(1f, fill = false))
                    CategoryGlyph(item, accent, Modifier.padding(start = 6.dp))
                    DirectionArrow(direction, accent)
                }
                PriorityMark(item, accent)
            }

            Text(
                text = personLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = returnLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 2.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status = status)
                if (item.note.isNotBlank()) {
                    Icon(
                        imageVector = Icons.Filled.NoteAlt,
                        contentDescription = "Has a note",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemName(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name.ifBlank { "Untitled item" },
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
private fun DirectionArrow(direction: ItemDirection, accent: Color) {
    val icon = if (direction == ItemDirection.Given) {
        Icons.AutoMirrored.Filled.ArrowForward
    } else {
        Icons.AutoMirrored.Filled.ArrowBack
    }
    val desc = if (direction == ItemDirection.Given) "Given, outgoing" else "Borrowed, incoming"
    Icon(
        imageVector = icon,
        contentDescription = desc,
        tint = accent,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun CategoryGlyph(item: BorrowItem, accent: Color, modifier: Modifier = Modifier) {
    Icon(
        imageVector = categoryIcon(item.category),
        contentDescription = "Category ${item.categoryLabel}",
        tint = accent,
        modifier = modifier.size(16.dp).padding(start = 4.dp)
    )
}

@Composable
private fun PriorityMark(item: BorrowItem, accent: Color) {
    if (item.priority == ItemPriority.High) {
        Icon(
            imageVector = Icons.Filled.PriorityHigh,
            contentDescription = "High priority",
            tint = accent,
            modifier = Modifier.size(16.dp)
        )
    }
}
