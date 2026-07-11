package com.borrownest.app.util

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemCategory
import com.borrownest.app.model.ItemDirection

/** Filters available on the Archive screen. */
sealed interface ArchiveFilter {
    data object All : ArchiveFilter
    data object Given : ArchiveFilter
    data object Borrowed : ArchiveFilter
    data class Person(val name: String) : ArchiveFilter
    data class Category(val category: ItemCategory) : ArchiveFilter
    data class Year(val year: Int) : ArchiveFilter
}

object ArchiveUtils {

    fun apply(items: List<BorrowItem>, filter: ArchiveFilter): List<BorrowItem> {
        val archived = items.filter { it.archived }
        return when (filter) {
            ArchiveFilter.All -> archived
            ArchiveFilter.Given -> archived.filter { it.direction == ItemDirection.Given }
            ArchiveFilter.Borrowed -> archived.filter { it.direction == ItemDirection.Borrowed }
            is ArchiveFilter.Person ->
                archived.filter { PersonUtils.normalize(it.personName) == PersonUtils.normalize(filter.name) }
            is ArchiveFilter.Category -> archived.filter { it.category == filter.category }
            is ArchiveFilter.Year -> archived.filter {
                DateUtils.year(it.actualReturnDate) == filter.year ||
                    DateUtils.year(it.recordDate) == filter.year
            }
        }
    }

    fun archivedYears(items: List<BorrowItem>): List<Int> =
        items.filter { it.archived }
            .mapNotNull { DateUtils.year(it.actualReturnDate) ?: DateUtils.year(it.recordDate) }
            .distinct()
            .sortedDescending()
}
