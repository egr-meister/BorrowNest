package com.borrownest.app

import com.borrownest.app.data.SerializationConfig
import com.borrownest.app.model.AppSettings
import com.borrownest.app.model.BorrowItem
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SerializationTest {

    private val json = SerializationConfig.json

    @Test fun itemRoundTrip() {
        val item = BorrowItem(id = "x", itemName = "Drill", personName = "Alex",
            recordDate = "2026-07-01")
        val encoded = json.encodeToString(BorrowItem.serializer(), item)
        val decoded = json.decodeFromString(BorrowItem.serializer(), encoded)
        assertEquals(item, decoded)
    }

    @Test fun missingFieldsUseDefaults() {
        // Older JSON with only some fields still decodes.
        val partial = """{"id":"y","itemName":"Book"}"""
        val decoded = json.decodeFromString(BorrowItem.serializer(), partial)
        assertEquals("y", decoded.id)
        assertEquals("Book", decoded.itemName)
        assertEquals("", decoded.personName)
    }

    @Test fun unknownKeysIgnored() {
        val extra = """{"id":"z","itemName":"Pen","futureField":123}"""
        val decoded = json.decodeFromString(BorrowItem.serializer(), extra)
        assertEquals("z", decoded.id)
    }

    @Test fun settingsDefaults() {
        val decoded = json.decodeFromString(AppSettings.serializer(), "{}")
        assertEquals(3, decoded.soonThresholdDays)
        assertTrue(decoded.reminderSettings.enabled)
    }

    @Test fun listOfItems() {
        val list = listOf(BorrowItem(id = "1"), BorrowItem(id = "2"))
        val encoded = json.encodeToString(ListSerializer(BorrowItem.serializer()), list)
        val decoded = json.decodeFromString(ListSerializer(BorrowItem.serializer()), encoded)
        assertEquals(2, decoded.size)
    }
}
