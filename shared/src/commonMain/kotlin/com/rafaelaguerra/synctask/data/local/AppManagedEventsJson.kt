package com.rafaelaguerra.synctask.data.local

import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object AppManagedEventsJson {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(events: List<AppManagedEvent>): String {
        return json.encodeToString(ListSerializer(AppManagedEvent.serializer()), events)
    }

    fun decode(raw: String?): List<AppManagedEvent> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(AppManagedEvent.serializer()), raw)
        }.getOrDefault(emptyList())
    }
}
