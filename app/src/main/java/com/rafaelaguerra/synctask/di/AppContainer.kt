package com.rafaelaguerra.synctask.di

import android.content.Context
import com.rafaelaguerra.synctask.data.billing.PlayBillingDataSource
import com.rafaelaguerra.synctask.data.calendar.CalendarLocalDataSource
import com.rafaelaguerra.synctask.data.device.PhoneStateAlarmScheduler
import com.rafaelaguerra.synctask.data.local.AppManagedEventsLocalDataSource
import com.rafaelaguerra.synctask.data.local.PremiumPreferencesLocalDataSource
import com.rafaelaguerra.synctask.data.remote.AppVersionGatekeeper
import com.rafaelaguerra.synctask.data.update.InAppUpdateCoordinator
import com.rafaelaguerra.synctask.data.repository.EventSyncRepositoryImpl
import com.rafaelaguerra.synctask.data.repository.PremiumRepositoryImpl
import com.rafaelaguerra.synctask.domain.repository.EventSyncRepository
import com.rafaelaguerra.synctask.domain.repository.PremiumRepository
import com.rafaelaguerra.synctask.domain.usecase.CanCreateEventThisWeekUseCase
import com.rafaelaguerra.synctask.domain.usecase.CanEditExistingEventModeUseCase
import com.rafaelaguerra.synctask.domain.usecase.CleanupExpiredManagedEventsUseCase
import com.rafaelaguerra.synctask.domain.usecase.CreateCalendarEventUseCase
import com.rafaelaguerra.synctask.domain.usecase.GetPremiumPriceUseCase
import com.rafaelaguerra.synctask.domain.usecase.GetAppManagedEventsUseCase
import com.rafaelaguerra.synctask.domain.usecase.ObservePremiumStatusUseCase
import com.rafaelaguerra.synctask.domain.usecase.RefreshPremiumStatusUseCase
import com.rafaelaguerra.synctask.domain.usecase.RemoveAppManagedEventUseCase
import com.rafaelaguerra.synctask.domain.usecase.SaveAppManagedEventUseCase
import com.rafaelaguerra.synctask.domain.usecase.SchedulePhoneStateForEventUseCase
import com.rafaelaguerra.synctask.domain.usecase.UpdateAppManagedEventPhoneStateUseCase

class AppContainer(
    context: Context
) {
    private val appContext = context.applicationContext

    private val calendarLocalDataSource = CalendarLocalDataSource(appContext.contentResolver)
    private val phoneStateAlarmScheduler = PhoneStateAlarmScheduler(appContext)
    private val appManagedEventsLocalDataSource = AppManagedEventsLocalDataSource(appContext)
    private val premiumPreferencesLocalDataSource = PremiumPreferencesLocalDataSource(appContext)

    val playBillingDataSource = PlayBillingDataSource(
        context = appContext,
        premiumPreferences = premiumPreferencesLocalDataSource
    )
    val appVersionGatekeeper = AppVersionGatekeeper(appContext)
    val inAppUpdateCoordinator = InAppUpdateCoordinator(appContext)

    private val eventSyncRepository: EventSyncRepository = EventSyncRepositoryImpl(
        calendarLocalDataSource = calendarLocalDataSource,
        phoneStateAlarmScheduler = phoneStateAlarmScheduler,
        appManagedEventsLocalDataSource = appManagedEventsLocalDataSource
    )
    private val premiumRepository: PremiumRepository = PremiumRepositoryImpl(
        playBillingDataSource = playBillingDataSource,
        premiumPreferencesLocalDataSource = premiumPreferencesLocalDataSource
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
