package com.borrownest.app.util

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.DerivedItemStatus
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState

/** A local, derived summary of records grouped by manually entered person name. */
data class PersonSummary(
    val displayName: String,
    val activeGiven: Int,
    val activeBorrowed: Int,
    val overdue: Int,
    val returnedCount: Int,
    val totalRecords: Int,
    val latestRecordDate: String
)

object PersonUtils {

    /** Normalization used ONLY for grouping. The display value is preserved. */
    fun normalize(name: String): String = name.trim().lowercase()

    fun sanitizeName(raw: String, maxLength: Int = 100): String =
        raw.trim().take(maxLength)

    /**
     * Groups items by normalized person name without merging distinct names.
     * "Alex" and "Alexander" stay separate. The most recent display spelling wins.
     */
    fun summarize(
        items: List<BorrowItem>,
        soonThresholdDays: Int
    ): List<PersonSummary> {
        val groups = items.filter { it.personName.isNotBlank() }
            .groupBy { normalize(it.personName) }

        return groups.map { (_, records) ->
            val display = records.maxByOrNull { it.updatedAt.ifBlank { it.createdAt } }
                ?.personName?.trim().orEmpty()

            var activeGiven = 0
            var activeBorrowed = 0
            var overdue = 0
            var returned = 0

            records.forEach { item ->
                val status = StatusUtils.derive(item, soonThresholdDays)
                val isActive = item.lifecycleState == ItemLifecycleState.Active && !item.archived
                if (item.lifecycleState == ItemLifecycleState.Returned) returned++
                if (status == DerivedItemStatus.Overdue) overdue++
                if (isActive) {
                    when (item.direction) {
                        ItemDirection.Given -> activeGiven++
                        ItemDirection.Borrowed -> activeBorrowed++
                    }
                }
            }

            val latest = records.mapNotNull { DateUtils.parse(it.recordDate) }.maxOrNull()

            PersonSummary(
                displayName = display,
                activeGiven = activeGiven,
                activeBorrowed = activeBorrowed,
                overdue = overdue,
                returnedCount = returned,
                totalRecords = records.size,
                latestRecordDate = latest?.let { DateUtils.format(it) } ?: ""
            )
        }.sortedBy { it.displayName.lowercase() }
    }
}
