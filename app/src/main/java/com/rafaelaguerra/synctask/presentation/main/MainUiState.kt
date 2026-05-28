package com.rafaelaguerra.synctask.presentation.main

import com.rafaelaguerra.synctask.presentation.common.UiText
import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.model.RecurrenceConfig
import com.rafaelaguerra.synctask.domain.model.RepeatPeriod
import java.util.Calendar

private const val ONE_HOUR_MILLIS = 60 * 60 * 1000L

data class MainUiState(
    // ─── Navigation ──────────────────────────────────────────────────────────
    val isCreateEventVisible: Boolean = false,
    val showPaywall: Boolean = false,
    val paywallReason: PaywallReason? = null,

    // ─── Event list ──────────────────────────────────────────────────────────
    val isEventsLoading: Boolean = false,
    val appManagedEvents: List<AppManagedEvent> = emptyList(),
    val selectedFilter: PhoneState? = null,
    val isPremium: Boolean = false,
    val premiumPriceLabel: String = "$0.99",
    val isPurchaseInProgress: Boolean = false,

    // ─── Create event form ────────────────────────────────────────────────────
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val startDateTimeMillis: Long = nextHalfHourMillis(),
    val endDateTimeMillis: Long = nextHalfHourMillis() + ONE_HOUR_MILLIS,
    val selectedPhoneState: PhoneState = PhoneState.DO_NOT_DISTURB,

    // ─── Recurrence ───────────────────────────────────────────────────────────
    val isRecurring: Boolean = false,
    val recurrenceDays: Set<Int> = emptySet(),
    val recurrencePeriod: RepeatPeriod = RepeatPeriod.WEEKLY,

    // ─── Async state ─────────────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val userMessage: UiText? = null,
    val createdEventPreview: CreatedEventPreview? = null
)

data class CreatedEventPreview(
    val eventId: Long,
    val title: String,
    val description: String,
    val location: String,
    val startDateTimeMillis: Long,
    val endDateTimeMillis: Long,
    val phoneState: PhoneState,
    val isRecurring: Boolean = false,
    val createdAtMillis: Long
)

private fun nextHalfHourMillis(nowMillis: Long = System.currentTimeMillis()): Long {
    return Calendar.getInstance().apply {
        timeInMillis = nowMillis
        val currentMinute = get(Calendar.MINUTE)
        if (currentMinute < 30) {
            set(Calendar.MINUTE, 30)
        } else {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
        }
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
