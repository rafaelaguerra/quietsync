package com.rafaelaguerra.synctask.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelaguerra.synctask.domain.model.AppManagedEvent
import com.rafaelaguerra.synctask.domain.model.CalendarEvent
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.model.RecurrenceConfig
import com.rafaelaguerra.synctask.domain.model.RepeatPeriod
import com.rafaelaguerra.synctask.domain.model.Weekday
import com.rafaelaguerra.synctask.domain.usecase.CanCreateEventThisWeekUseCase
import com.rafaelaguerra.synctask.domain.usecase.CanEditExistingEventModeUseCase
import com.rafaelaguerra.synctask.domain.usecase.CleanupExpiredManagedEventsUseCase
import com.rafaelaguerra.synctask.domain.usecase.CreateCalendarEventUseCase
import com.rafaelaguerra.synctask.domain.usecase.GetAppManagedEventsUseCase
import com.rafaelaguerra.synctask.domain.usecase.GetPremiumPriceUseCase
import com.rafaelaguerra.synctask.domain.usecase.ObservePremiumStatusUseCase
import com.rafaelaguerra.synctask.domain.usecase.RefreshPremiumStatusUseCase
import com.rafaelaguerra.synctask.domain.usecase.RemoveAppManagedEventUseCase
import com.rafaelaguerra.synctask.domain.usecase.SaveAppManagedEventUseCase
import com.rafaelaguerra.synctask.domain.usecase.SchedulePhoneStateForEventUseCase
import com.rafaelaguerra.synctask.domain.usecase.UpdateAppManagedEventPhoneStateUseCase
import com.rafaelaguerra.synctask.presentation.common.UiText
import com.rafaelaguerra.synctask.presentation.common.toUiText
import com.rafaelaguerra.synctask.resources.Res
import com.rafaelaguerra.synctask.resources.msg_conflict_existing_event
import com.rafaelaguerra.synctask.resources.msg_edit_mode_premium
import com.rafaelaguerra.synctask.resources.msg_end_after_start
import com.rafaelaguerra.synctask.resources.msg_select_weekday
import com.rafaelaguerra.synctask.resources.msg_title_required
import com.rafaelaguerra.synctask.resources.msg_weekly_limit_reached
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val createCalendarEventUseCase: CreateCalendarEventUseCase,
    private val schedulePhoneStateForEventUseCase: SchedulePhoneStateForEventUseCase,
    private val saveAppManagedEventUseCase: SaveAppManagedEventUseCase,
    private val getAppManagedEventsUseCase: GetAppManagedEventsUseCase,
    private val cleanupExpiredManagedEventsUseCase: CleanupExpiredManagedEventsUseCase,
    private val removeAppManagedEventUseCase: RemoveAppManagedEventUseCase,
    private val updateAppManagedEventPhoneStateUseCase: UpdateAppManagedEventPhoneStateUseCase,
    private val observePremiumStatusUseCase: ObservePremiumStatusUseCase,
    private val refreshPremiumStatusUseCase: RefreshPremiumStatusUseCase,
    private val getPremiumPriceUseCase: GetPremiumPriceUseCase,
    private val canCreateEventThisWeekUseCase: CanCreateEventThisWeekUseCase,
    private val canEditExistingEventModeUseCase: CanEditExistingEventModeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observePremiumStatus()
        refreshPremiumStatus(showErrors = false)
        loadPremiumPrice()
        loadAppManagedEvents()
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    fun showCreateEvent() {
        // Always start the form empty. Best-practice UX: don't leak previously typed
        // values into a fresh "Create new event" flow.
        val suggestedStart = nextHalfHourMillis()
        _uiState.update { current ->
            current.copy(
                isCreateEventVisible = true,
                createdEventPreview = null,
                showPaywall = false,
                paywallReason = null,
                title = "",
                description = "",
                location = "",
                isRecurring = false,
                recurrenceDays = emptySet(),
                recurrencePeriod = RepeatPeriod.WEEKLY,
                startDateTimeMillis = suggestedStart,
                endDateTimeMillis = suggestedStart + ONE_HOUR_MILLIS,
                selectedPhoneState = PhoneState.DO_NOT_DISTURB
            )
        }
    }

    fun onCreateEventEntryRequested() {
        val current = _uiState.value
        if (!canCreateEventThisWeekUseCase(current.isPremium, current.appManagedEvents)) {
            showPaywall(PaywallReason.WEEKLY_LIMIT)
            showMessage(UiText.Resource(Res.string.msg_weekly_limit_reached))
            return
        }
        showCreateEvent()
    }

    fun hideCreateEvent() {
        _uiState.update { it.copy(isCreateEventVisible = false) }
    }

    fun goToEventList() {
        _uiState.update {
            it.copy(
                isCreateEventVisible = false,
                createdEventPreview = null,
                showPaywall = false,
                paywallReason = null
            )
        }
    }

    fun showPaywall(reason: PaywallReason) {
        _uiState.update {
            it.copy(
                showPaywall = true,
                paywallReason = reason
            )
        }
    }

    fun hidePaywall() {
        _uiState.update { it.copy(showPaywall = false, paywallReason = null) }
    }

    // ─── Filter ───────────────────────────────────────────────────────────────

    fun onFilterSelected(filter: PhoneState?) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    // ─── Form fields ─────────────────────────────────────────────────────────

    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onLocationChange(value: String) = _uiState.update { it.copy(location = value) }

    fun onStartDateTimeChange(value: Long) {
        _uiState.update { current ->
            val end = if (value >= current.endDateTimeMillis) value + 3_600_000L
                      else current.endDateTimeMillis
            current.copy(startDateTimeMillis = value, endDateTimeMillis = end)
        }
    }

    fun onEndDateTimeChange(value: Long) = _uiState.update { it.copy(endDateTimeMillis = value) }
    fun onPhoneStateSelected(state: PhoneState) = _uiState.update { it.copy(selectedPhoneState = state) }

    // ─── Recurrence ───────────────────────────────────────────────────────────

    fun onRecurringToggled(enabled: Boolean) = _uiState.update { it.copy(isRecurring = enabled) }

    fun onRecurrenceDayToggled(weekday: Weekday) {
        _uiState.update { current ->
            val days = current.recurrenceDays.toMutableSet()
            if (!days.remove(weekday)) days.add(weekday)
            current.copy(recurrenceDays = days)
        }
    }

    fun onRecurrencePeriodSelected(period: RepeatPeriod) =
        _uiState.update { it.copy(recurrencePeriod = period) }

    // ─── Messages ────────────────────────────────────────────────────────────

    fun showMessage(message: UiText) = _uiState.update { it.copy(userMessage = message) }
    fun clearMessage() = _uiState.update { it.copy(userMessage = null) }

    fun setPurchaseInProgress(inProgress: Boolean) {
        _uiState.update { it.copy(isPurchaseInProgress = inProgress) }
    }

    // ─── Create another event ────────────────────────────────────────────────

    fun onCreateAnotherEvent() {
        val suggestedStart = nextHalfHourMillis()
        _uiState.update { current ->
            current.copy(
                isCreateEventVisible = true,
                createdEventPreview = null,
                title = "",
                description = "",
                location = "",
                isRecurring = false,
                recurrenceDays = emptySet(),
                recurrencePeriod = RepeatPeriod.WEEKLY,
                startDateTimeMillis = suggestedStart,
                endDateTimeMillis = suggestedStart + ONE_HOUR_MILLIS,
                selectedPhoneState = PhoneState.DO_NOT_DISTURB,
                showPaywall = false,
                paywallReason = null
            )
        }
    }

    // ─── Event list ──────────────────────────────────────────────────────────

    fun loadAppManagedEvents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isEventsLoading = true) }
            cleanupExpiredManagedEventsUseCase()
            getAppManagedEventsUseCase()
                .onSuccess { events ->
                    _uiState.update { it.copy(isEventsLoading = false, appManagedEvents = events) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isEventsLoading = false, userMessage = error.toUiText())
                    }
                }
        }
    }

    fun removeAppManagedEvent(eventId: Long) {
        viewModelScope.launch {
            removeAppManagedEventUseCase(eventId)
                .onSuccess {
                    loadAppManagedEvents()
                    _uiState.update { current ->
                        if (current.createdEventPreview?.eventId == eventId) {
                            current.copy(createdEventPreview = null)
                        } else current
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(userMessage = error.toUiText()) }
                }
        }
    }

    fun updateAppManagedEventPhoneState(
        eventId: Long,
        phoneState: PhoneState
    ) {
        val current = _uiState.value
        val selectedEvent = current.appManagedEvents.firstOrNull { it.eventId == eventId } ?: return

        if (selectedEvent.phoneState == phoneState) return

        if (!canEditExistingEventModeUseCase(current.isPremium)) {
            showPaywall(PaywallReason.EDIT_EXISTING_EVENT)
            showMessage(UiText.Resource(Res.string.msg_edit_mode_premium))
            return
        }

        val conflict = findModeConflict(
            existingEvents = current.appManagedEvents,
            newStartDateTimeMillis = selectedEvent.startDateTimeMillis,
            newEndDateTimeMillis = selectedEvent.endDateTimeMillis,
            newPhoneState = phoneState,
            excludedEventId = eventId
        )
        if (conflict != null) {
            showMessage(
                UiText.Resource(
                    Res.string.msg_conflict_existing_event,
                    conflict.title,
                    conflict.phoneState
                )
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            updateAppManagedEventPhoneStateUseCase(eventId = eventId, phoneState = phoneState)
                .onSuccess {
                    loadAppManagedEvents()
                    _uiState.update { uiState ->
                        val updatedPreview =
                            if (uiState.createdEventPreview?.eventId == eventId) {
                                uiState.createdEventPreview.copy(phoneState = phoneState)
                            } else {
                                uiState.createdEventPreview
                            }
                        uiState.copy(
                            isLoading = false,
                            createdEventPreview = updatedPreview
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, userMessage = error.toUiText())
                    }
                }
        }
    }

    // ─── Create event ─────────────────────────────────────────────────────────

    fun createEventAndScheduleState() {
        val current = _uiState.value
        val eventTitle = current.title.trim()

        if (eventTitle.isEmpty()) {
            showMessage(UiText.Resource(Res.string.msg_title_required))
            return
        }
        if (current.endDateTimeMillis <= current.startDateTimeMillis) {
            showMessage(UiText.Resource(Res.string.msg_end_after_start))
            return
        }
        if (!canCreateEventThisWeekUseCase(current.isPremium, current.appManagedEvents)) {
            showPaywall(PaywallReason.WEEKLY_LIMIT)
            showMessage(UiText.Resource(Res.string.msg_weekly_limit_reached))
            return
        }
        val conflict = findModeConflict(
            existingEvents = current.appManagedEvents,
            newStartDateTimeMillis = current.startDateTimeMillis,
            newEndDateTimeMillis = current.endDateTimeMillis,
            newPhoneState = current.selectedPhoneState
        )
        if (conflict != null) {
            showMessage(
                UiText.Resource(
                    Res.string.msg_conflict_existing_event,
                    conflict.title,
                    conflict.phoneState
                )
            )
            return
        }
        if (current.isRecurring && current.recurrencePeriod == RepeatPeriod.WEEKLY && current.recurrenceDays.isEmpty()) {
            showMessage(UiText.Resource(Res.string.msg_select_weekday))
            return
        }

        val recurrenceConfig = if (current.isRecurring) {
            RecurrenceConfig(
                daysOfWeek = current.recurrenceDays,
                period = current.recurrencePeriod
            )
        } else null

        val event = CalendarEvent(
            title = eventTitle,
            description = current.description.trim(),
            location = current.location.trim(),
            startDateTimeMillis = current.startDateTimeMillis,
            endDateTimeMillis = current.endDateTimeMillis,
            phoneState = current.selectedPhoneState,
            recurrenceConfig = recurrenceConfig
        )
        val createdAtMillis = currentTimeMillis()

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, userMessage = null) }

            createCalendarEventUseCase(event)
                .onSuccess { eventId ->
                    schedulePhoneStateForEventUseCase(
                        eventId = eventId,
                        startDateTimeMillis = current.startDateTimeMillis,
                        endDateTimeMillis = current.endDateTimeMillis,
                        phoneState = current.selectedPhoneState
                    ).onSuccess {
                        val storedEvents = saveAppManagedEventUseCase(
                            eventId = eventId,
                            title = event.title,
                            phoneState = current.selectedPhoneState,
                            startDateTimeMillis = current.startDateTimeMillis,
                            endDateTimeMillis = current.endDateTimeMillis,
                            createdAtMillis = createdAtMillis
                        ).fold(
                            onSuccess = { getAppManagedEventsUseCase().getOrDefault(current.appManagedEvents) },
                            onFailure = { current.appManagedEvents }
                        )

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                appManagedEvents = storedEvents,
                                isCreateEventVisible = false,
                                createdEventPreview = CreatedEventPreview(
                                    eventId = eventId,
                                    title = event.title,
                                    description = event.description,
                                    location = event.location,
                                    startDateTimeMillis = event.startDateTimeMillis,
                                    endDateTimeMillis = event.endDateTimeMillis,
                                    phoneState = current.selectedPhoneState,
                                    isRecurring = current.isRecurring,
                                    createdAtMillis = createdAtMillis
                                )
                            )
                        }
                    }.onFailure { error ->
                        _uiState.update { it.copy(isLoading = false, userMessage = error.toUiText()) }
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, userMessage = error.toUiText()) }
                }
        }
    }

    fun refreshPremiumStatus(showErrors: Boolean) {
        viewModelScope.launch {
            refreshPremiumStatusUseCase()
                .onSuccess { isPremium ->
                    _uiState.update { it.copy(isPremium = isPremium) }
                }
                .onFailure { error ->
                    if (showErrors) showMessage(error.toUiText())
                }
        }
    }

    fun loadPremiumPrice() {
        viewModelScope.launch {
            getPremiumPriceUseCase()
                .onSuccess { price ->
                    _uiState.update { it.copy(premiumPriceLabel = price) }
                }
        }
    }

    private fun observePremiumStatus() {
        viewModelScope.launch {
            observePremiumStatusUseCase().collectLatest { isPremium ->
                _uiState.update { it.copy(isPremium = isPremium) }
            }
        }
    }
}

private const val ONE_HOUR_MILLIS = 60 * 60 * 1000L

private fun findModeConflict(
    existingEvents: List<AppManagedEvent>,
    newStartDateTimeMillis: Long,
    newEndDateTimeMillis: Long,
    newPhoneState: PhoneState,
    excludedEventId: Long? = null
): AppManagedEvent? {
    return existingEvents.firstOrNull { existing ->
        if (existing.startDateTimeMillis <= 0L || existing.endDateTimeMillis <= 0L) {
            false
        } else if (excludedEventId != null && existing.eventId == excludedEventId) {
            false
        } else {
            existing.phoneState != newPhoneState &&
                intervalsOverlap(
                    startA = existing.startDateTimeMillis,
                    endA = existing.endDateTimeMillis,
                    startB = newStartDateTimeMillis,
                    endB = newEndDateTimeMillis
                )
        }
    }
}

private fun intervalsOverlap(
    startA: Long,
    endA: Long,
    startB: Long,
    endB: Long
): Boolean {
    return startA < endB && startB < endA
}
