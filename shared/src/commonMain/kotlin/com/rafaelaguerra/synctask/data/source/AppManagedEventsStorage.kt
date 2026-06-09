package com.rafaelaguerra.synctask.data.source

import com.rafaelaguerra.synctask.domain.model.AppManagedEvent

interface AppManagedEventsStorage {
    fun save(event: AppManagedEvent)
    fun getAll(): List<AppManagedEvent>
    fun remove(eventId: Long)
}
