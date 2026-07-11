package com.borrownest.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.borrownest.app.data.BorrowRepository
import com.borrownest.app.model.AppData
import com.borrownest.app.model.BoardFilter
import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemCategory
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ReminderSettings
import com.borrownest.app.util.ItemFilter
import com.borrownest.app.util.ItemSort
import com.borrownest.app.util.Reminder
import com.borrownest.app.util.ReminderUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single shared ViewModel for the whole app. Keeps architecture simple and
 * stable: one repository, one observable state, StateFlow for observation.
 */
class BorrowViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = BorrowRepository(app.applicationContext)

    // Transient UI selections (search, filters, sort, view mode).
    private val selections = MutableStateFlow(SelectionState())

    private data class SelectionState(
        val searchQuery: String = "",
        val activeFilter: ItemFilter = ItemFilter.All,
        val categoryFilter: ItemCategory? = null,
        val sort: ItemSort = ItemSort.NearestReturn,
        val boardListMode: Boolean = false
    )

    val uiState: StateFlow<BorrowUiState> =
        combine(repository.appData, selections) { data: AppData, sel ->
            BorrowUiState(
                loaded = true,
                data = data,
                searchQuery = sel.searchQuery,
                activeFilter = sel.activeFilter,
                categoryFilter = sel.categoryFilter,
                sort = sel.sort,
                boardListMode = sel.boardListMode
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BorrowUiState()
        )

    /** In-app reminders recomputed from the latest state. */
    val reminders: StateFlow<List<Reminder>> =
        repository.appData.let { flow ->
            combine(flow, selections) { data, _ ->
                ReminderUtils.evaluate(
                    items = data.items,
                    settings = data.settings.reminderSettings,
                    soonThresholdDays = data.settings.soonThresholdDays
                )
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        }

    fun itemById(id: String?): BorrowItem? =
        id?.let { uiState.value.data.items.firstOrNull { item -> item.id == it } }

    // ---- Selection mutations ----

    fun setSearchQuery(q: String) = selections.update { it.copy(searchQuery = q) }
    fun setActiveFilter(f: ItemFilter) = selections.update { it.copy(activeFilter = f) }
    fun setCategoryFilter(c: ItemCategory?) = selections.update { it.copy(categoryFilter = c) }
    fun setSort(s: ItemSort) = selections.update { it.copy(sort = s) }
    fun toggleBoardListMode() = selections.update { it.copy(boardListMode = !it.boardListMode) }
    fun clearFilters() = selections.update {
        it.copy(activeFilter = ItemFilter.All, categoryFilter = null, searchQuery = "")
    }

    // ---- Item operations (fire-and-forget on the VM scope) ----

    fun addItem(draft: BorrowItem, onDone: (BorrowItem) -> Unit = {}) = viewModelScope.launch {
        val created = repository.addItem(draft)
        onDone(created)
    }

    fun updateItem(item: BorrowItem) = viewModelScope.launch { repository.updateItem(item) }
    fun deleteItem(id: String) = viewModelScope.launch { repository.deleteItem(id) }
    fun markReturned(id: String, date: String, note: String?) =
        viewModelScope.launch { repository.markReturned(id, date, note) }
    fun undoReturn(id: String) = viewModelScope.launch { repository.undoReturn(id) }
    fun archiveItem(id: String) = viewModelScope.launch { repository.archiveItem(id) }
    fun restoreItem(id: String) = viewModelScope.launch { repository.restoreItem(id) }
    fun permanentlyDelete(id: String) = viewModelScope.launch { repository.permanentlyDelete(id) }

    // ---- Settings operations ----

    fun completeOnboarding() = viewModelScope.launch { repository.completeOnboarding() }
    fun showOnboardingAgain() = viewModelScope.launch { repository.setOnboardingCompleted(false) }

    fun setSoonThreshold(days: Int) =
        viewModelScope.launch { repository.updateSettings { it.copy(soonThresholdDays = days) } }

    fun setDefaultBoardFilter(filter: BoardFilter) =
        viewModelScope.launch { repository.updateSettings { it.copy(defaultBoardFilter = filter) } }

    fun updateReminderSettings(transform: (ReminderSettings) -> ReminderSettings) =
        viewModelScope.launch {
            repository.updateSettings { it.copy(reminderSettings = transform(it.reminderSettings)) }
        }

    // ---- Destructive bulk operations ----

    fun archiveAllReturned() = viewModelScope.launch { repository.archiveAllReturned() }
    fun clearArchive() = viewModelScope.launch { repository.clearArchive() }
    fun deleteAllGiven() = viewModelScope.launch { repository.deleteAllByDirection(ItemDirection.Given) }
    fun deleteAllBorrowed() =
        viewModelScope.launch { repository.deleteAllByDirection(ItemDirection.Borrowed) }
    fun resetAllData() = viewModelScope.launch { repository.resetAll() }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as Application
                return BorrowViewModel(app) as T
            }
        }
    }
}
