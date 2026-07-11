package com.borrownest.app

import com.borrownest.app.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {
    private val today = LocalDate.of(2026, 7, 10)

    @Test fun parseValid() {
        assertEquals(LocalDate.of(2026, 7, 10), DateUtils.parse("2026-07-10"))
    }

    @Test fun parseInvalidReturnsNull() {
        assertNull(DateUtils.parse("2026-13-40"))
        assertNull(DateUtils.parse(""))
        assertNull(DateUtils.parse("garbage"))
    }

    @Test fun daysUntil() {
        assertEquals(5L, DateUtils.daysUntil("2026-07-15", today))
        assertEquals(-2L, DateUtils.daysUntil("2026-07-08", today))
    }

    @Test fun daysOverdue() {
        assertEquals(2L, DateUtils.daysOverdue("2026-07-08", today))
        assertNull(DateUtils.daysOverdue("2026-07-20", today))
    }

    @Test fun durationDays() {
        assertEquals(9L, DateUtils.durationDays("2026-07-01", "2026-07-10"))
        assertNull(DateUtils.durationDays("2026-07-10", "2026-07-01"))
    }

    @Test fun yearMonthKey() {
        assertEquals("2026-07", DateUtils.yearMonthKey("2026-07-10"))
        assertNull(DateUtils.yearMonthKey("bad"))
    }

    @Test fun validity() {
        assertTrue(DateUtils.isValid("2026-01-01"))
        assertFalse(DateUtils.isValid("nope"))
    }
}
