package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CanCreateEventThisWeekUseCaseTest {

    private val useCase = CanCreateEventThisWeekUseCase()

    @Test
    fun premiumUsersCanAlwaysCreate() {
        assertTrue(
            useCase.invoke(
                isPremium = true,
                events = List(10) { sampleEvent(createdAtMillis = 1_700_000_000_000L) },
                nowMillis = 1_700_000_000_000L
            )
        )
    }

    @Test
    fun freeUsersAreLimitedToThreeEventsPerWeek() {
        val now = 1_704_067_200_000L // Monday 2024-01-01 UTC-ish window; week math uses local TZ
        val events = List(3) { index ->
            sampleEvent(createdAtMillis = now + index)
        }

        assertFalse(
            useCase.invoke(
                isPremium = false,
                events = events,
                nowMillis = now
            )
        )
    }

    private fun sampleEvent(createdAtMillis: Long) = AppManagedEvent(
        eventId = 1L,
        title = "Focus",
        phoneState = PhoneState.DO_NOT_DISTURB,
        startDateTimeMillis = createdAtMillis,
        endDateTimeMillis = createdAtMillis + 3_600_000L,
        createdAtMillis = createdAtMillis
    )
}
