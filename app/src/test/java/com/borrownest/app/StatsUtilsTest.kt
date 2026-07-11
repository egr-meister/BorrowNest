package com.borrownest.app

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.util.StatsUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StatsUtilsTest {
    private val today = LocalDate.of(2026, 7, 10)

    @Test fun monthlyReturnedHasSixBuckets() {
        val months = StatsUtils.monthlyReturned(emptyList(), today)
        assertEquals(6, months.size)
    }

    @Test fun countsReturnedThisMonth() {
        val items = listOf(
            BorrowItem(id = "1", direction = ItemDirection.Given, itemName = "A",
                personName = "P", recordDate = "2026-07-01",
                actualReturnDate = "2026-07-05",
                lifecycleState = ItemLifecycleState.Returned)
        )
        val stats = StatsUtils.compute(items, 3, today)
        assertEquals(1, stats.returnedThisMonth)
    }

    @Test fun countsActiveByDirection() {
        val items = listOf(
            BorrowItem(id = "g", direction = ItemDirection.Given, itemName = "A",
                personName = "P", recordDate = "2026-07-01", expectedReturnDate = "2026-07-30"),
            BorrowItem(id = "b", direction = ItemDirection.Borrowed, itemName = "B",
                personName = "P", recordDate = "2026-07-01", expectedReturnDate = "2026-07-30")
        )
        val stats = StatsUtils.compute(items, 3, today)
        assertEquals(1, stats.activeGiven)
        assertEquals(1, stats.activeBorrowed)
    }
}
