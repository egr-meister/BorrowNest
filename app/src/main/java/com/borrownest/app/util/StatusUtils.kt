package com.borrownest.app.util

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.DerivedItemStatus
import com.borrownest.app.model.ItemLifecycleState
import java.time.LocalDate

/**
 * Derives the calculated status of an item. Never persisted, always computed.
 *
 * Order:
 *  1. archived           -> Archived
 *  2. lifecycle Returned -> Returned
 *  3. no return date     -> NoReturnDate
 *  4. return date invalid-> InvalidDate
 *  5. before today       -> Overdue
 *  6. equals today       -> DueToday
 *  7. within threshold   -> DueSoon
 *  8. otherwise          -> Active
 */
object StatusUtils {

    fun derive(
        item: BorrowItem,
        soonThresholdDays: Int,
        today: LocalDate = DateUtils.today()
    ): DerivedItemStatus {
        if (item.archived) return DerivedItemStatus.Archived
        if (item.lifecycleState == ItemLifecycleState.Returned) return DerivedItemStatus.Returned

        if (item.expectedReturnDate.isBlank()) return DerivedItemStatus.NoReturnDate

        val due = DateUtils.parse(item.expectedReturnDate)
            ?: return DerivedItemStatus.InvalidDate

        return when {
            due.isBefore(today) -> DerivedItemStatus.Overdue
            due.isEqual(today) -> DerivedItemStatus.DueToday
            else -> {
                val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, due)
                if (daysUntil in 1..soonThresholdDays.toLong()) DerivedItemStatus.DueSoon
                else DerivedItemStatus.Active
            }
        }
    }

    /** True when the derived status represents an item that still needs attention. */
    fun isActiveLike(status: DerivedItemStatus): Boolean = when (status) {
        DerivedItemStatus.Active,
        DerivedItemStatus.DueSoon,
        DerivedItemStatus.DueToday,
        DerivedItemStatus.Overdue,
        DerivedItemStatus.NoReturnDate,
        DerivedItemStatus.InvalidDate -> true
        DerivedItemStatus.Returned,
        DerivedItemStatus.Archived -> false
    }

    /** Ranking used for board sorting: lower rank = shown higher / more urgent. */
    fun urgencyRank(status: DerivedItemStatus): Int = when (status) {
        DerivedItemStatus.Overdue -> 0
        DerivedItemStatus.DueToday -> 1
        DerivedItemStatus.DueSoon -> 2
        DerivedItemStatus.Active -> 3
        DerivedItemStatus.NoReturnDate -> 4
        DerivedItemStatus.InvalidDate -> 5
        DerivedItemStatus.Returned -> 6
        DerivedItemStatus.Archived -> 7
    }
}
