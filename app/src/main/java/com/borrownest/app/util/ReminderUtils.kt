package com.borrownest.app.util

import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.DerivedItemStatus
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.model.ItemPriority
import com.borrownest.app.model.ReminderSettings

enum class ReminderKind { Overdue, DueToday, DueSoon, NoReturnDate }

/** A single in-app reminder. No push notifications are ever used. */
data class Reminder(
    val itemId: String,
    val kind: ReminderKind,
    val title: String,
    val detail: String,
    val highPriority: Boolean
)

object ReminderUtils {

    /**
     * Evaluate in-app reminders for the active items. Ordering: overdue first,
     * high priority raised within each group, then due today, due soon, no date.
     */
    fun evaluate(
        items: List<BorrowItem>,
        settings: ReminderSettings,
        soonThresholdDays: Int
    ): List<Reminder> {
        if (!settings.enabled) return emptyList()

        val reminders = mutableListOf<Reminder>()

        items.filter {
            it.lifecycleState == ItemLifecycleState.Active && !it.archived
        }.forEach { item ->
            val status = StatusUtils.derive(item, soonThresholdDays)
            val kind = when (status) {
                DerivedItemStatus.Overdue -> ReminderKind.Overdue
                DerivedItemStatus.DueToday -> ReminderKind.DueToday
                DerivedItemStatus.DueSoon -> ReminderKind.DueSoon
                DerivedItemStatus.NoReturnDate -> ReminderKind.NoReturnDate
                else -> null
            } ?: return@forEach

            val enabled = when (kind) {
                ReminderKind.Overdue -> settings.showOverdue
                ReminderKind.DueToday -> settings.showDueToday
                ReminderKind.DueSoon -> settings.showDueSoon
                ReminderKind.NoReturnDate -> settings.showNoReturnDate
            }
            if (!enabled) return@forEach

            reminders += Reminder(
                itemId = item.id,
                kind = kind,
                title = buildTitle(item, kind),
                detail = buildDetail(item, kind),
                highPriority = item.priority == ItemPriority.High
            )
        }

        return reminders.sortedWith(
            compareBy<Reminder> { it.kind.ordinal }
                .thenByDescending { it.highPriority }
        )
    }

    private fun buildTitle(item: BorrowItem, kind: ReminderKind): String {
        val name = item.itemName.ifBlank { "Item" }
        return when (kind) {
            ReminderKind.Overdue -> "$name is overdue."
            ReminderKind.DueToday -> when (item.direction) {
                ItemDirection.Given -> "$name is expected back today."
                ItemDirection.Borrowed -> "Return the $name."
            }
            ReminderKind.DueSoon -> "$name is due soon."
            ReminderKind.NoReturnDate -> "$name has no return date."
        }
    }

    private fun buildDetail(item: BorrowItem, kind: ReminderKind): String {
        val personPart = when (item.direction) {
            ItemDirection.Given -> "Given to ${item.personName.ifBlank { "someone" }}."
            ItemDirection.Borrowed -> "Borrowed from ${item.personName.ifBlank { "someone" }}."
        }
        val datePart = when (kind) {
            ReminderKind.Overdue ->
                "Expected back on ${DateUtils.displayDateShort(item.expectedReturnDate)}."
            ReminderKind.DueToday -> "Due today."
            ReminderKind.DueSoon ->
                "Due ${DateUtils.displayDateShort(item.expectedReturnDate)}."
            ReminderKind.NoReturnDate -> "Consider adding an expected return date."
        }
        return "$personPart $datePart"
    }
}
