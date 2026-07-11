package com.borrownest.app.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.borrownest.app.BuildConfig
import com.borrownest.app.model.AppData
import com.borrownest.app.model.AppSettings
import com.borrownest.app.model.BorrowItem
import com.borrownest.app.model.ItemDirection
import com.borrownest.app.model.ItemHistoryEvent
import com.borrownest.app.model.ItemHistoryEventType
import com.borrownest.app.model.ItemLifecycleState
import com.borrownest.app.util.DateUtils
import com.borrownest.app.util.IdUtils
import com.borrownest.app.util.PersonUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "borrownest")

/**
 * The single local repository. Backed by DataStore Preferences storing three
 * serialized JSON strings: items, history events and settings.
 *
 * All reads are defensive. Corrupted JSON never crashes the app; the corrupted
 * slice falls back to a safe default while preserving the others where possible.
 */
class BorrowRepository(private val context: Context) {

    private companion object {
        const val TAG = "BorrowRepository"
        val ITEMS_KEY = stringPreferencesKey("items_json")
        val HISTORY_KEY = stringPreferencesKey("item_history_json")
        val SETTINGS_KEY = stringPreferencesKey("settings_json")
    }

    private val json = SerializationConfig.json

    /** Combined, always-safe view of all app data as a Flow. */
    val appData: Flow<AppData> = context.dataStore.data
        .catch { e ->
            // IOException from a corrupt preferences file: emit empty and continue.
            if (e is IOException) {
                Log.w(TAG, "DataStore read failed; using empty preferences.")
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map { prefs -> decode(prefs) }

    private fun decode(prefs: Preferences): AppData {
        val items = decodeItems(prefs[ITEMS_KEY])
        val history = decodeHistory(prefs[HISTORY_KEY])
        val settings = decodeSettings(prefs[SETTINGS_KEY])
        return AppData(items = items, historyEvents = history, settings = settings)
    }

    private fun decodeItems(raw: String?): List<BorrowItem> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(BorrowItem.serializer()), raw)
                .filter { it.id.isNotBlank() }
        } catch (e: Exception) {
            logDecodeError("items")
            // Item-level recovery: try to salvage individual valid objects.
            salvageItems(raw)
        }
    }

    private fun salvageItems(raw: String): List<BorrowItem> {
        return try {
            val element = json.parseToJsonElement(raw)
            val array = (element as? kotlinx.serialization.json.JsonArray) ?: return emptyList()
            array.mapNotNull { el ->
                try {
                    json.decodeFromJsonElement(BorrowItem.serializer(), el)
                        .takeIf { it.id.isNotBlank() }
                } catch (inner: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun decodeHistory(raw: String?): List<ItemHistoryEvent> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(ListSerializer(ItemHistoryEvent.serializer()), raw)
                .filter { it.id.isNotBlank() }
        } catch (e: Exception) {
            logDecodeError("history")
            emptyList()
        }
    }

    private fun decodeSettings(raw: String?): AppSettings {
        if (raw.isNullOrBlank()) return AppSettings()
        return try {
            json.decodeFromString(AppSettings.serializer(), raw)
        } catch (e: Exception) {
            logDecodeError("settings")
            AppSettings()
        }
    }

    private fun logDecodeError(section: String) {
        // Never log the raw JSON, person names or notes.
        if (BuildConfig.DEBUG) {
            Log.w(TAG, "Failed to decode stored $section; falling back to safe default.")
        }
    }

    private suspend fun currentData(): AppData = appData.first()

    private suspend fun persist(data: AppData) {
        context.dataStore.edit { prefs ->
            prefs[ITEMS_KEY] =
                json.encodeToString(ListSerializer(BorrowItem.serializer()), data.items)
            prefs[HISTORY_KEY] =
                json.encodeToString(ListSerializer(ItemHistoryEvent.serializer()), data.historyEvents)
            prefs[SETTINGS_KEY] =
                json.encodeToString(AppSettings.serializer(), data.settings)
        }
    }

    // ---- History helpers ----

    private fun historyEvent(
        itemId: String,
        type: ItemHistoryEventType,
        description: String
    ): ItemHistoryEvent {
        val today = DateUtils.today()
        return ItemHistoryEvent(
            id = IdUtils.newId(),
            itemId = itemId,
            eventType = type,
            eventDate = DateUtils.format(today),
            eventTime = DateUtils.currentTimeHhmm(),
            description = description,
            createdAt = DateUtils.nowTimestamp()
        )
    }

    suspend fun addHistoryEvent(event: ItemHistoryEvent) {
        val data = currentData()
        persist(data.copy(historyEvents = data.historyEvents + event))
    }

    // ---- Settings ----

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val data = currentData()
        persist(data.copy(settings = transform(data.settings)))
    }

    suspend fun completeOnboarding() =
        updateSettings { it.copy(onboardingCompleted = true) }

    suspend fun setOnboardingCompleted(value: Boolean) =
        updateSettings { it.copy(onboardingCompleted = value) }

    // ---- Item CRUD ----

    /** Creates a new record, generating an id and a Created history event. */
    suspend fun addItem(draft: BorrowItem): BorrowItem {
        val now = DateUtils.nowTimestamp()
        val item = draft.copy(
            id = IdUtils.newId(),
            personName = PersonUtils.sanitizeName(draft.personName),
            itemName = draft.itemName.trim(),
            note = draft.note,
            lifecycleState = ItemLifecycleState.Active,
            archived = false,
            actualReturnDate = "",
            createdAt = now,
            updatedAt = now
        )
        val data = currentData()
        val event = historyEvent(
            item.id,
            ItemHistoryEventType.Created,
            "${item.direction.label} record created for \"${item.itemName}\"."
        )
        persist(
            data.copy(
                items = data.items + item,
                historyEvents = data.historyEvents + event
            )
        )
        return item
    }

    suspend fun updateItem(updated: BorrowItem) {
        val data = currentData()
        if (data.items.none { it.id == updated.id }) return
        val clean = updated.copy(
            personName = PersonUtils.sanitizeName(updated.personName),
            itemName = updated.itemName.trim(),
            updatedAt = DateUtils.nowTimestamp()
        )
        val event = historyEvent(
            clean.id,
            ItemHistoryEventType.Updated,
            "Record updated for \"${clean.itemName}\"."
        )
        persist(
            data.copy(
                items = data.items.map { if (it.id == clean.id) clean else it },
                historyEvents = data.historyEvents + event
            )
        )
    }

    suspend fun deleteItem(itemId: String) {
        val data = currentData()
        persist(
            data.copy(
                items = data.items.filterNot { it.id == itemId },
                historyEvents = data.historyEvents.filterNot { it.itemId == itemId }
            )
        )
    }

    suspend fun markReturned(itemId: String, actualReturnDate: String, finalNote: String?) {
        val data = currentData()
        val item = data.items.firstOrNull { it.id == itemId } ?: return
        val returnDate = actualReturnDate.ifBlank { DateUtils.format(DateUtils.today()) }
        val updated = item.copy(
            lifecycleState = ItemLifecycleState.Returned,
            actualReturnDate = returnDate,
            note = finalNote?.takeIf { it.isNotBlank() } ?: item.note,
            updatedAt = DateUtils.nowTimestamp()
        )
        val verb = if (item.direction == ItemDirection.Given) "received back" else "returned"
        val event = historyEvent(
            itemId,
            ItemHistoryEventType.MarkedReturned,
            "\"${item.itemName}\" marked $verb on ${DateUtils.displayDateShort(returnDate)}."
        )
        persist(
            data.copy(
                items = data.items.map { if (it.id == itemId) updated else it },
                historyEvents = data.historyEvents + event
            )
        )
    }

    suspend fun undoReturn(itemId: String) {
        val data = currentData()
        val item = data.items.firstOrNull { it.id == itemId } ?: return
        val updated = item.copy(
            lifecycleState = ItemLifecycleState.Active,
            actualReturnDate = "",
            archived = false,
            updatedAt = DateUtils.nowTimestamp()
        )
        val event = historyEvent(
            itemId,
            ItemHistoryEventType.ReturnUndone,
            "Return undone for \"${item.itemName}\"; moved back to active."
        )
        persist(
            data.copy(
                items = data.items.map { if (it.id == itemId) updated else it },
                historyEvents = data.historyEvents + event
            )
        )
    }

    suspend fun archiveItem(itemId: String) {
        val data = currentData()
        val item = data.items.firstOrNull { it.id == itemId } ?: return
        val updated = item.copy(archived = true, updatedAt = DateUtils.nowTimestamp())
        val event = historyEvent(
            itemId,
            ItemHistoryEventType.Archived,
            "\"${item.itemName}\" archived."
        )
        persist(
            data.copy(
                items = data.items.map { if (it.id == itemId) updated else it },
                historyEvents = data.historyEvents + event
            )
        )
    }

    suspend fun restoreItem(itemId: String) {
        val data = currentData()
        val item = data.items.firstOrNull { it.id == itemId } ?: return
        val updated = item.copy(archived = false, updatedAt = DateUtils.nowTimestamp())
        val event = historyEvent(
            itemId,
            ItemHistoryEventType.Restored,
            "\"${item.itemName}\" restored from archive."
        )
        persist(
            data.copy(
                items = data.items.map { if (it.id == itemId) updated else it },
                historyEvents = data.historyEvents + event
            )
        )
    }

    suspend fun permanentlyDelete(itemId: String) = deleteItem(itemId)

    // ---- Bulk operations ----

    suspend fun archiveAllReturned() {
        val data = currentData()
        val now = DateUtils.nowTimestamp()
        val toArchive = data.items.filter {
            it.lifecycleState == ItemLifecycleState.Returned && !it.archived
        }
        if (toArchive.isEmpty()) return
        val events = toArchive.map {
            historyEvent(it.id, ItemHistoryEventType.Archived, "\"${it.itemName}\" archived.")
        }
        persist(
            data.copy(
                items = data.items.map {
                    if (it.lifecycleState == ItemLifecycleState.Returned && !it.archived) {
                        it.copy(archived = true, updatedAt = now)
                    } else it
                },
                historyEvents = data.historyEvents + events
            )
        )
    }

    suspend fun clearArchive() {
        val data = currentData()
        val archivedIds = data.items.filter { it.archived }.map { it.id }.toSet()
        persist(
            data.copy(
                items = data.items.filterNot { it.archived },
                historyEvents = data.historyEvents.filterNot { it.itemId in archivedIds }
            )
        )
    }

    suspend fun deleteAllByDirection(direction: ItemDirection) {
        val data = currentData()
        val ids = data.items.filter { it.direction == direction }.map { it.id }.toSet()
        persist(
            data.copy(
                items = data.items.filterNot { it.direction == direction },
                historyEvents = data.historyEvents.filterNot { it.itemId in ids }
            )
        )
    }

    /** Wipes all local data back to defaults. */
    suspend fun resetAll() {
        context.dataStore.edit { it.clear() }
    }
}
