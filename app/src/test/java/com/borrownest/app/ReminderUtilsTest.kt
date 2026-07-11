package com.borrownest.app

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.model.ItemPriority
import com.borrownest.app.model.ReminderSettings
import com.borrownest.app.util.DateUtils
import com.borrownest.app.util.ReminderKind
import com.borrownest.app.util.ReminderUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderUtilsTest {

    private val today = DateUtils.today()

    private fun item(id: String, offsetDays: Long, priority: ItemPriority = ItemPriority.Normal) =
        BorrowItem(
            id = id, direction = ItemDirection.Given, itemName = "Item $id",
            personName = "Jordan", recordDate = DateUtils.format(today.minusDays(10)),
            expectedReturnDate = DateUtils.format(today.plusDays(offsetDays)),
            lifecycleState = ItemLifecycleState.Active, priority = priority
        )

    @Test fun disabledReturnsEmpty() {
        val r = ReminderUtils.evaluate(
            listOf(item("a", -2)),
            ReminderSettings(enabled = false),
            3
        )
        assertTrue(r.isEmpty())
    }

    @Test fun overdueFirstAndHighPriorityRaised() {
        val items = listOf(
            item("soon", 2),
            item("overdueNormal", -1),
            item("overdueHigh", -1, ItemPriority.High)
        )
        val r = ReminderUtils.evaluate(items, ReminderSettings(), 3)
        assertEquals(ReminderKind.Overdue, r.first().kind)
        assertEquals("overdueHigh", r.first().itemId)
    }

    @Test fun respectsPerTypeToggles() {
        val items = listOf(item("soon", 2))
        val r = ReminderUtils.evaluate(items, ReminderSettings(showDueSoon = false), 3)
        assertTrue(r.none { it.kind == ReminderKind.DueSoon })
    }
}
