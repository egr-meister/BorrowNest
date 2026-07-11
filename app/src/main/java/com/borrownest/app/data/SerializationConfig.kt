package com.borrownest.app.data

import kotlinx.serialization.json.Json

/** Single lenient, backward-compatible JSON configuration used for all storage. */
object SerializationConfig {
    val json: Json = Json {
        ignoreUnknownKeys = true      // tolerate fields removed in future versions
        encodeDefaults = true         // always write defaults so reads are stable
        isLenient = true
        coerceInputValues = true      // fall back to defaults for null/invalid enum values
        explicitNulls = false
    }
}
