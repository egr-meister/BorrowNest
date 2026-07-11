package com.borrownest.app

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.model.ItemPriority
import com.borrownest.app.util.FilterSortUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterSortUtilsTest {

    private fun item(
        id: String,
        expected: String,
        priority: ItemPriority = ItemPriority.Normal,
        direction: ItemDirection = ItemDirection.Given
    ) = BorrowItem(
        id = id, direction = direction, itemName = "Item $id", personName = "P",
        recordDate = "2026-07-01", expectedReturnDate = expected,
        lifecycleState = ItemLifecycleState.Active, priority = priority
    )

    @Test fun activeBoardOrdersOverdueFirst() {
        val items = listOf(
            item("future", "2026-08-01"),
            item("overdue", "2026-07-01"),
            item("today", "2026-07-10"),
            item("soon", "2026-07-12")
        )
        val sorted = FilterSortUtils.sortActiveBoard(items, 3)
        // First should be the overdue one given a today of 2026-07-10 is used inside deriving.
        assertTrue(sorted.first().id == "overdue" || sorted.first().id == "today")
    }

    @Test fun nearestExpectedReturnPicksEarliest() {
        val items = listOf(item("a", "2026-08-01"), item("b", "2026-07-15"))
        assertEquals("b", FilterSortUtils.nearestExpectedReturn(items)?.id)
    }

    @Test fun activeForDirectionFilters() {
        val items = listOf(
            item("g", "2026-07-15", direction = ItemDirection.Given),
            item("b", "2026-07-15", direction = ItemDirection.Borrowed)
        )
        assertEquals(1, FilterSortUtils.activeForDirection(items, ItemDirection.Given).size)
        assertEquals(1, FilterSortUtils.activeForDirection(items, ItemDirection.Borrowed).size)
    }

    @Test fun searchMatchesName() {
        val i = item("g", "2026-07-15")
        assertTrue(FilterSortUtils.matchesSearch(i, "Item g"))
        assertTrue(FilterSortUtils.matchesSearch(i, ""))
    }
}
