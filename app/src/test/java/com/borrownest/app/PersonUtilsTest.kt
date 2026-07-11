package com.borrownest.app

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.util.PersonUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonUtilsTest {

    private fun item(person: String, direction: ItemDirection = ItemDirection.Given) =
        BorrowItem(
            id = person + direction, direction = direction, itemName = "X",
            personName = person, recordDate = "2026-07-01",
            expectedReturnDate = "2026-07-20", lifecycleState = ItemLifecycleState.Active
        )

    @Test fun similarNamesNotMerged() {
        val summaries = PersonUtils.summarize(listOf(item("Alex"), item("Alexander")), 3)
        assertEquals(2, summaries.size)
    }

    @Test fun caseInsensitiveGrouping() {
        val summaries = PersonUtils.summarize(listOf(item("Alex"), item("alex")), 3)
        assertEquals(1, summaries.size)
    }

    @Test fun countsGivenAndBorrowed() {
        val summaries = PersonUtils.summarize(
            listOf(item("Sam", ItemDirection.Given), item("Sam", ItemDirection.Borrowed)),
            3
        )
        assertEquals(1, summaries.size)
        assertEquals(1, summaries.first().activeGiven)
        assertEquals(1, summaries.first().activeBorrowed)
    }

    @Test fun sanitizeTrimsAndLimits() {
        assertEquals("Neighbor", PersonUtils.sanitizeName("  Neighbor  "))
        assertEquals(100, PersonUtils.sanitizeName("a".repeat(200)).length)
    }
}
