package com.rafaelaguerra.synctask.di

import com.rafaelaguerra.synctask.data.repository.EventSyncRepositoryImpl
import com.rafaelaguerra.synctask.data.repository.PremiumRepositoryImpl
import com.rafaelaguerra.synctask.data.source.AppManagedEventsStorage
import com.rafaelaguerra.synctask.data.source.CalendarDataSource
import com.rafaelaguerra.synctask.data.source.PhoneStateScheduler
import com.rafaelaguerra.synctask.data.source.PremiumBillingDataSource
import com.rafaelaguerra.synctask.data.source.PremiumPreferencesStorage
import com.rafaelaguerra.synctask.domain.error.ErrorTracker
import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository
import com.rafaelaguerra.synctask.domain.repository.PremiumRepository
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

class SharedContainer(
    calendarDataSource: CalendarDataSource,
    phoneStateScheduler: PhoneStateScheduler,
    appManagedEventsStorage: AppManagedEventsStorage,
    premiumBillingDataSource: PremiumBillingDataSource,
    premiumPreferencesStorage: PremiumPreferencesStorage,
    val errorTracker: ErrorTracker = ErrorTracker.NoOp
) {
    private val eventSyncRepository: EventSyncRepository = EventSyncRepositoryImpl(
        calendarDataSource = calendarDataSource,
        phoneStateScheduler = phoneStateScheduler,
        appManagedEventsStorage = appManagedEventsStorage
    )
    private val premiumRepository: PremiumRepository = PremiumRepositoryImpl(
        premiumBillingDataSource = premiumBillingDataSource,
        premiumPreferencesStorage = premiumPreferencesStorage
    )

    val createCalendarEventUseCase = CreateCalendarEventUseCase(eventSyncRepository)
    val schedulePhoneStateForEventUseCase = SchedulePhoneStateForEventUseCase(eventSyncRepository)
    val saveAppManagedEventUseCase = SaveAppManagedEventUseCase(eventSyncRepository)
    val getAppManagedEventsUseCase = GetAppManagedEventsUseCase(eventSyncRepository)
    val cleanupExpiredManagedEventsUseCase = CleanupExpiredManagedEventsUseCase(eventSyncRepository)
    val removeAppManagedEventUseCase = RemoveAppManagedEventUseCase(eventSyncRepository)
    val updateAppManagedEventPhoneStateUseCase = UpdateAppManagedEventPhoneStateUseCase(eventSyncRepository)

    val observePremiumStatusUseCase = ObservePremiumStatusUseCase(premiumRepository)
    val refreshPremiumStatusUseCase = RefreshPremiumStatusUseCase(premiumRepository)
    val getPremiumPriceUseCase = GetPremiumPriceUseCase(premiumRepository)

    val canCreateEventThisWeekUseCase = CanCreateEventThisWeekUseCase()
    val canEditExistingEventModeUseCase = CanEditExistingEventModeUseCase()
}
