package com.borrownest.app.util

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.DerivedItemStatus
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import java.time.LocalDate

data class MonthlyReturnedCount(val monthKey: String, val label: String, val count: Int)

/** Neutral, local summary statistics. No person scoring of any kind. */
data class BorrowStatistics(
    val activeGiven: Int,
    val activeBorrowed: Int,
    val overdueGiven: Int,
    val overdueBorrowed: Int,
    val dueToday: Int,
    val noReturnDate: Int,
    val returnedThisMonth: Int,
    val archivedTotal: Int,
    val mostUsedCategory: String,
    val averageActiveDurationDays: Long?,
    val monthlyReturned: List<MonthlyReturnedCount>
) {
    val activeGivenBorrowedRatio: Pair<Int, Int> get() = activeGiven to activeBorrowed
}

object StatsUtils {

    fun compute(
        items: List<BorrowItem>,
        soonThresholdDays: Int,
        today: LocalDate = DateUtils.today()
    ): BorrowStatistics {
        var activeGiven = 0
        var activeBorrowed = 0
        var overdueGiven = 0
        var overdueBorrowed = 0
        var dueToday = 0
        var noReturnDate = 0
        var archived = 0

        items.forEach { item ->
            if (item.archived) { archived++; return@forEach }
            val status = StatusUtils.derive(item, soonThresholdDays, today)
            val active = item.lifecycleState == ItemLifecycleState.Active
            if (active) {
                when (item.direction) {
                    ItemDirection.Given -> activeGiven++
                    ItemDirection.Borrowed -> activeBorrowed++
                }
            }
            when (status) {
                DerivedItemStatus.Overdue -> when (item.direction) {
                    ItemDirection.Given -> overdueGiven++
                    ItemDirection.Borrowed -> overdueBorrowed++
                }
                DerivedItemStatus.DueToday -> dueToday++
                DerivedItemStatus.NoReturnDate -> noReturnDate++
                else -> {}
            }
        }

        val thisMonthKey = "%04d-%02d".format(today.year, today.monthValue)
        val returnedThisMonth = items.count {
            it.lifecycleState == ItemLifecycleState.Returned &&
                DateUtils.yearMonthKey(it.actualReturnDate) == thisMonthKey
        }

        val mostUsedCategory = items
            .groupingBy { it.categoryLabel }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key ?: "—"

        val durations = items.mapNotNull { item ->
            if (item.lifecycleState == ItemLifecycleState.Returned) {
                DateUtils.durationDays(item.recordDate, item.actualReturnDate)
            } else {
                DateUtils.durationDays(item.recordDate, DateUtils.format(today))
            }
        }
        val avgDuration = if (durations.isNotEmpty()) durations.average().toLong() else null

        return BorrowStatistics(
            activeGiven = activeGiven,
            activeBorrowed = activeBorrowed,
            overdueGiven = overdueGiven,
            overdueBorrowed = overdueBorrowed,
            dueToday = dueToday,
            noReturnDate = noReturnDate,
            returnedThisMonth = returnedThisMonth,
            archivedTotal = archived,
            mostUsedCategory = mostUsedCategory,
            averageActiveDurationDays = avgDuration,
            monthlyReturned = monthlyReturned(items, today)
        )
    }

    /** Returned counts for the last 6 months (including current), built for Compose columns. */
    fun monthlyReturned(
        items: List<BorrowItem>,
        today: LocalDate = DateUtils.today()
    ): List<MonthlyReturnedCount> {
        val months = (5 downTo 0).map { today.minusMonths(it.toLong()) }
        return months.map { date ->
            val key = "%04d-%02d".format(date.year, date.monthValue)
            val count = items.count {
                it.lifecycleState == ItemLifecycleState.Returned &&
                    DateUtils.yearMonthKey(it.actualReturnDate) == key
            }
            MonthlyReturnedCount(
                monthKey = key,
                label = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                count = count
            )
        }
    }
}
