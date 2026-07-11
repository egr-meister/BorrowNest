package com.borrownest.app.ui.viewmodel

import com.borrownest.app.model.AppData
import com.borrownest.app.model.AppSettings
import com.borrownest.app.model.ItemCategory
import com.borrownest.app.util.ItemFilter
import com.borrownest.app.util.ItemSort

/** Immutable top-level UI state derived from stored data + transient selections. */
data class BorrowUiState(
    val loaded: Boolean = false,
    val data: AppData = AppData(),
    val searchQuery: String = "",
    val activeFilter: ItemFilter = ItemFilter.All,
    val categoryFilter: ItemCategory? = null,
    val sort: ItemSort = ItemSort.NearestReturn,
    val boardListMode: Boolean = false
) {
    val settings: AppSettings get() = data.settings
    val soonThresholdDays: Int get() = data.settings.soonThresholdDays
}
