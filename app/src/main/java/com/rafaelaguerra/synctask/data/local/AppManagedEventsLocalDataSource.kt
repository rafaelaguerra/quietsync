package com.rafaelaguerra.synctask.data.local

import android.content.Context
import com.rafaelaguerra.synctask.data.source.AppManagedEventsStorage
import com.rafaelaguerra.synctask.domain.model.AppManagedEvent

class AppManagedEventsLocalDataSource(
    context: Context
) : AppManagedEventsStorage {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override fun save(event: AppManagedEvent) {
        val currentEvents = getAll().toMutableList()
        currentEvents.removeAll { it.eventId == event.eventId }
        currentEvents.add(0, event)
        persist(currentEvents)
    }

    override fun getAll(): List<AppManagedEvent> {
        return AppManagedEventsJson.decode(preferences.getString(KEY_EVENTS_JSON, null))
    }

    override fun remove(eventId: Long) {
        persist(getAll().filterNot { it.eventId == eventId })
    }

    private fun persist(events: List<AppManagedEvent>) {
        preferences.edit()
            .putString(KEY_EVENTS_JSON, AppManagedEventsJson.encode(events))
            .apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "app_managed_events_store"
        private const val KEY_EVENTS_JSON = "key_events_json"
    }
}
