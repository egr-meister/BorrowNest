package com.borrownest.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Direction of a record.
 * Given  = the user gave an item to another person.
 * Borrowed = the user borrowed an item from another person.
 */
@Serializable
enum class ItemDirection {
    @SerialName("Given") Given,
    @SerialName("Borrowed") Borrowed;

    val label: String
        get() = when (this) {
            Given -> "Given"
            Borrowed -> "Borrowed"
        }
}

/** Fixed set of item categories. "Other" allows a manual custom label. */
@Serializable
enum class ItemCategory {
    @SerialName("Book") Book,
    @SerialName("Tool") Tool,
    @SerialName("Clothing") Clothing,
    @SerialName("Electronics") Electronics,
    @SerialName("Household") Household,
    @SerialName("Sports") Sports,
    @SerialName("Document") Document,
    @SerialName("Accessory") Accessory,
    @SerialName("Other") Other;

    val label: String get() = name
}

/** Persistent lifecycle state. Date-based statuses are derived, not stored here. */
@Serializable
enum class ItemLifecycleState {
    @SerialName("Active") Active,
    @SerialName("Returned") Returned
}

@Serializable
enum class ItemPriority {
    @SerialName("Normal") Normal,
    @SerialName("High") High
}

@Serializable
enum class ItemHistoryEventType {
    @SerialName("Created") Created,
    @SerialName("Updated") Updated,
    @SerialName("MarkedReturned") MarkedReturned,
    @SerialName("ReturnUndone") ReturnUndone,
    @SerialName("Archived") Archived,
    @SerialName("Restored") Restored
}

/** Filter controlling which lanes appear on the board. */
@Serializable
enum class BoardFilter {
    @SerialName("Both") Both,
    @SerialName("Given") Given,
    @SerialName("Borrowed") Borrowed
}

/**
 * Derived, calculated status of an item. This is NEVER persisted; it is
 * computed from the stored lifecycle state, archived flag and dates.
 */
enum class DerivedItemStatus {
    Active,
    DueSoon,
    DueToday,
    Overdue,
    NoReturnDate,
    Returned,
    Archived,
    InvalidDate;

    val label: String
        get() = when (this) {
            Active -> "Active"
            DueSoon -> "Due soon"
            DueToday -> "Due today"
            Overdue -> "Overdue"
            NoReturnDate -> "No return date"
            Returned -> "Returned"
            Archived -> "Archived"
            InvalidDate -> "Return date unavailable"
        }
}

/**
 * A single borrowed/given item record.
 *
 * Dates are stored as ISO calendar strings (YYYY-MM-DD). Empty string means
 * "not set". createdAt / updatedAt are ISO-8601 timestamps.
 *
 * All fields have defaults so that older stored JSON that is missing a field
 * still deserializes cleanly (backward-compatible deserialization).
 */
@Serializable
data class BorrowItem(
    val id: String = "",
    val direction: ItemDirection = ItemDirection.Given,
    val itemName: String = "",
    val category: ItemCategory = ItemCategory.Other,
    val customCategoryName: String = "",
    val personName: String = "",
    val recordDate: String = "",
    val expectedReturnDate: String = "",
    val actualReturnDate: String = "",
    val lifecycleState: ItemLifecycleState = ItemLifecycleState.Active,
    val priority: ItemPriority = ItemPriority.Normal,
    val note: String = "",
    val archived: Boolean = false,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    /** Display category label, honoring a custom name for the Other category. */
    val categoryLabel: String
        get() = if (category == ItemCategory.Other && customCategoryName.isNotBlank()) {
            customCategoryName.trim()
        } else {
            category.label
        }
}

/** An explicit, append-only history event for an item. */
@Serializable
data class ItemHistoryEvent(
    val id: String = "",
    val itemId: String = "",
    val eventType: ItemHistoryEventType = ItemHistoryEventType.Created,
    val eventDate: String = "",
    val eventTime: String = "",
    val description: String = "",
    val createdAt: String = ""
)

@Serializable
data class ReminderSettings(
    val enabled: Boolean = true,
    val showDueSoon: Boolean = true,
    val showDueToday: Boolean = true,
    val showOverdue: Boolean = true,
    val showNoReturnDate: Boolean = false
)

@Serializable
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val soonThresholdDays: Int = 3,
    val defaultBoardFilter: BoardFilter = BoardFilter.Both,
    val reminderSettings: ReminderSettings = ReminderSettings()
)

/** Top-level serialized container. Every field defaults for safe deserialization. */
@Serializable
data class AppData(
    val items: List<BorrowItem> = emptyList(),
    val historyEvents: List<ItemHistoryEvent> = emptyList(),
    val settings: AppSettings = AppSettings()
)
