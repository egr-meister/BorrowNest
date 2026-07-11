package com.borrownest.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.borrownest.app.model.DerivedItemStatus
import com.borrownest.app.model.ItemDirection

/** Maps a derived status to its accent color. */
fun statusColor(status: DerivedItemStatus): Color = when (status) {
    DerivedItemStatus.Active -> StatusActive
    DerivedItemStatus.DueSoon -> StatusDueSoon
    DerivedItemStatus.DueToday -> StatusDueToday
    DerivedItemStatus.Overdue -> StatusOverdue
    DerivedItemStatus.NoReturnDate -> StatusNoReturnDate
    DerivedItemStatus.Returned -> StatusReturned
    DerivedItemStatus.Archived -> StatusArchived
    DerivedItemStatus.InvalidDate -> StatusNoReturnDate
}

fun directionAccent(direction: ItemDirection): Color = when (direction) {
    ItemDirection.Given -> GivenTerracotta
    ItemDirection.Borrowed -> BorrowedIndigo
}

fun directionDeep(direction: ItemDirection): Color = when (direction) {
    ItemDirection.Given -> GivenDeep
    ItemDirection.Borrowed -> BorrowedDeep
}

fun directionSoft(direction: ItemDirection): Color = when (direction) {
    ItemDirection.Given -> GivenSoft
    ItemDirection.Borrowed -> BorrowedSoft
}
