package com.rafaelaguerra.synctask.data.local

import com.rafaelaguerra.synctask.data.source.AppManagedEventsStorage
import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import platform.Foundation.NSUserDefaults

class IosAppManagedEventsStorage : AppManagedEventsStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun save(event: AppManagedEvent) {
        val currentEvents = getAll().toMutableList()
        currentEvents.removeAll { it.eventId == event.eventId }
        currentEvents.add(0, event)
        persist(currentEvents)
    }

    override fun getAll(): List<AppManagedEvent> {
        return AppManagedEventsJson.decode(defaults.stringForKey(KEY_EVENTS_JSON))
    }

    override fun remove(eventId: Long) {
        persist(getAll().filterNot { it.eventId == eventId })
    }

    private fun persist(events: List<AppManagedEvent>) {
        defaults.setObject(AppManagedEventsJson.encode(events), KEY_EVENTS_JSON)
    }

    private companion object {
        const val KEY_EVENTS_JSON = "key_events_json"
    }
}
