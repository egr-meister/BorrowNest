package com.borrownest.app.util

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.DerivedItemStatus
import com.borrownest.app.model.ItemCategory
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.model.ItemPriority

/** The set of filters the user can apply to lists. */
enum class ItemFilter {
    All, Given, Borrowed, Active, DueSoon, DueToday, Overdue, NoReturnDate, Returned, HighPriority
}

enum class ItemSort {
    NearestReturn, MostOverdue, NewestCreated, OldestCreated, PersonName, ItemName
}

object FilterSortUtils {

    fun matchesSearch(item: BorrowItem, query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return item.itemName.lowercase().contains(q) ||
            item.personName.lowercase().contains(q) ||
            item.categoryLabel.lowercase().contains(q) ||
            item.note.lowercase().contains(q)
    }

    fun matchesFilter(
        item: BorrowItem,
        filter: ItemFilter,
        soonThresholdDays: Int
    ): Boolean {
        val status = StatusUtils.derive(item, soonThresholdDays)
        return when (filter) {
            ItemFilter.All -> true
            ItemFilter.Given -> item.direction == ItemDirection.Given
            ItemFilter.Borrowed -> item.direction == ItemDirection.Borrowed
            ItemFilter.Active -> StatusUtils.isActiveLike(status)
            ItemFilter.DueSoon -> status == DerivedItemStatus.DueSoon
            ItemFilter.DueToday -> status == DerivedItemStatus.DueToday
            ItemFilter.Overdue -> status == DerivedItemStatus.Overdue
            ItemFilter.NoReturnDate -> status == DerivedItemStatus.NoReturnDate
            ItemFilter.Returned -> item.lifecycleState == ItemLifecycleState.Returned
            ItemFilter.HighPriority -> item.priority == ItemPriority.High
        }
    }

    fun matchesCategory(item: BorrowItem, category: ItemCategory?): Boolean =
        category == null || item.category == category

    fun sort(items: List<BorrowItem>, sort: ItemSort): List<BorrowItem> = when (sort) {
        ItemSort.NearestReturn -> items.sortedWith(
            compareBy(nullsLast()) { DateUtils.parse(it.expectedReturnDate) }
        )
        ItemSort.MostOverdue -> items.sortedWith(
            compareBy(nullsLast()) { DateUtils.parse(it.expectedReturnDate) }
        )
        ItemSort.NewestCreated -> items.sortedByDescending { it.createdAt }
        ItemSort.OldestCreated -> items.sortedBy { it.createdAt }
        ItemSort.PersonName -> items.sortedBy { it.personName.lowercase() }
        ItemSort.ItemName -> items.sortedBy { it.itemName.lowercase() }
    }

    /**
     * Default active-board ordering:
     * overdue, due today, due soon, high priority, nearest return date, no return date.
     */
    fun sortActiveBoard(items: List<BorrowItem>, soonThresholdDays: Int): List<BorrowItem> {
        return items.sortedWith(
            compareBy<BorrowItem> {
                StatusUtils.urgencyRank(StatusUtils.derive(it, soonThresholdDays))
            }.thenByDescending {
                if (it.priority == ItemPriority.High) 1 else 0
            }.thenBy(nullsLast()) {
                DateUtils.parse(it.expectedReturnDate)
            }.thenBy { it.itemName.lowercase() }
        )
    }

    /** Active (non-returned, non-archived) items for one direction. */
    fun activeForDirection(
        items: List<BorrowItem>,
        direction: ItemDirection
    ): List<BorrowItem> = items.filter {
        it.direction == direction &&
            it.lifecycleState == ItemLifecycleState.Active &&
            !it.archived
    }

    /** The single nearest upcoming (or overdue) expected return across active items. */
    fun nearestExpectedReturn(items: List<BorrowItem>): BorrowItem? =
        items.filter {
            it.lifecycleState == ItemLifecycleState.Active &&
                !it.archived &&
                DateUtils.parse(it.expectedReturnDate) != null
        }.minByOrNull { DateUtils.parse(it.expectedReturnDate)!! }
}
