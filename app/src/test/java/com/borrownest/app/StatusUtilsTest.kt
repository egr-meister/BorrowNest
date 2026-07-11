package com.borrownest.app

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.DerivedItemStatus
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.util.StatusUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StatusUtilsTest {

    private val today = LocalDate.of(2026, 7, 10)

    private fun item(
        expected: String = "",
        lifecycle: ItemLifecycleState = ItemLifecycleState.Active,
        archived: Boolean = false,
        direction: ItemDirection = ItemDirection.Given
    ) = BorrowItem(
        id = "1",
        direction = direction,
        itemName = "Thing",
        personName = "Alex",
        recordDate = "2026-07-01",
        expectedReturnDate = expected,
        lifecycleState = lifecycle,
        archived = archived
    )

    @Test fun archivedTakesPrecedence() {
        val s = StatusUtils.derive(item(expected = "2026-07-05", archived = true), 3, today)
        assertEquals(DerivedItemStatus.Archived, s)
    }

    @Test fun returnedStatus() {
        val s = StatusUtils.derive(item(lifecycle = ItemLifecycleState.Returned), 3, today)
        assertEquals(DerivedItemStatus.Returned, s)
    }

    @Test fun noReturnDate() {
        val s = StatusUtils.derive(item(expected = ""), 3, today)
        assertEquals(DerivedItemStatus.NoReturnDate, s)
    }

    @Test fun overdue() {
        val s = StatusUtils.derive(item(expected = "2026-07-08"), 3, today)
        assertEquals(DerivedItemStatus.Overdue, s)
    }

    @Test fun dueToday() {
        val s = StatusUtils.derive(item(expected = "2026-07-10"), 3, today)
        assertEquals(DerivedItemStatus.DueToday, s)
    }

    @Test fun dueSoonWithinThreshold() {
        val s = StatusUtils.derive(item(expected = "2026-07-12"), 3, today)
        assertEquals(DerivedItemStatus.DueSoon, s)
    }

    @Test fun activeBeyondThreshold() {
        val s = StatusUtils.derive(item(expected = "2026-07-30"), 3, today)
        assertEquals(DerivedItemStatus.Active, s)
    }

    @Test fun thresholdChangeMovesActiveToDueSoon() {
        val i = item(expected = "2026-07-16")
        assertEquals(DerivedItemStatus.Active, StatusUtils.derive(i, 3, today))
        assertEquals(DerivedItemStatus.DueSoon, StatusUtils.derive(i, 7, today))
    }

    @Test fun invalidDate() {
        val s = StatusUtils.derive(item(expected = "not-a-date"), 3, today)
        assertEquals(DerivedItemStatus.InvalidDate, s)
    }
}
