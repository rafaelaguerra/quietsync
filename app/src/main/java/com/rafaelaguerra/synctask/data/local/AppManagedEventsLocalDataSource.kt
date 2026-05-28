package com.rafaelaguerra.synctask.data.local

import android.content.Context
import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import org.json.JSONArray
import org.json.JSONObject

class AppManagedEventsLocalDataSource(
    context: Context
) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    fun save(event: AppManagedEvent) {
        val currentEvents = getAll().toMutableList()
        currentEvents.removeAll { it.eventId == event.eventId }
        currentEvents.add(0, event)
        persist(currentEvents)
    }

    fun getAll(): List<AppManagedEvent> {
        val rawJson = preferences.getString(KEY_EVENTS_JSON, null) ?: return emptyList()
        val jsonArray = JSONArray(rawJson)
        val parsed = mutableListOf<AppManagedEvent>()

        for (index in 0 until jsonArray.length()) {
            val item = jsonArray.optJSONObject(index) ?: continue
            val eventId = item.optLong(KEY_EVENT_ID, -1L)
            val title = item.optString(KEY_TITLE, "")
            val phoneStateName = item.optString(KEY_PHONE_STATE, PhoneState.NORMAL.name)
            val startDateTimeMillis = item.optLong(KEY_START_DATE_TIME_MILLIS, 0L)
            val endDateTimeMillis = item.optLong(KEY_END_DATE_TIME_MILLIS, 0L)
            val createdAtMillis = item.optLong(KEY_CREATED_AT_MILLIS, startDateTimeMillis)
            val phoneState = runCatching { PhoneState.valueOf(phoneStateName) }
                .getOrDefault(PhoneState.NORMAL)

            if (eventId <= 0L || title.isBlank()) continue

            parsed.add(
                AppManagedEvent(
                    eventId = eventId,
                    title = title,
                    phoneState = phoneState,
                    startDateTimeMillis = startDateTimeMillis,
                    endDateTimeMillis = endDateTimeMillis,
                    createdAtMillis = createdAtMillis
                )
            )
        }
        return parsed
    }

    fun remove(eventId: Long) {
        val updated = getAll().filterNot { it.eventId == eventId }
        persist(updated)
    }

    private fun persist(events: List<AppManagedEvent>) {
        val jsonArray = JSONArray()
        events.forEach { event ->
            jsonArray.put(
                JSONObject().apply {
                    put(KEY_EVENT_ID, event.eventId)
                    put(KEY_TITLE, event.title)
                    put(KEY_PHONE_STATE, event.phoneState.name)
                    put(KEY_START_DATE_TIME_MILLIS, event.startDateTimeMillis)
                    put(KEY_END_DATE_TIME_MILLIS, event.endDateTimeMillis)
                    put(KEY_CREATED_AT_MILLIS, event.createdAtMillis)
                }
            )
        }
        preferences.edit().putString(KEY_EVENTS_JSON, jsonArray.toString()).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "app_managed_events_store"
        private const val KEY_EVENTS_JSON = "key_events_json"
        private const val KEY_EVENT_ID = "event_id"
        private const val KEY_TITLE = "title"
        private const val KEY_PHONE_STATE = "phone_state"
        private const val KEY_START_DATE_TIME_MILLIS = "start_date_time_millis"
        private const val KEY_END_DATE_TIME_MILLIS = "end_date_time_millis"
        private const val KEY_CREATED_AT_MILLIS = "created_at_millis"
    }
}
