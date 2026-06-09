package com.rafaelaguerra.synctask.di

import android.content.Context
import com.rafaelaguerra.synctask.data.billing.PlayBillingDataSource
import com.rafaelaguerra.synctask.data.calendar.CalendarLocalDataSource
import com.rafaelaguerra.synctask.data.device.PhoneStateAlarmScheduler
import com.rafaelaguerra.synctask.data.local.AppManagedEventsLocalDataSource
import com.rafaelaguerra.synctask.data.local.PremiumPreferencesLocalDataSource
import com.rafaelaguerra.synctask.data.remote.AppVersionGatekeeper
import com.rafaelaguerra.synctask.data.update.InAppUpdateCoordinator
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
import com.rafaelaguerra.synctask.presentation.main.createMainViewModel

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

    private val sharedContainer = SharedContainer(
        calendarDataSource = calendarLocalDataSource,
        phoneStateScheduler = phoneStateAlarmScheduler,
        appManagedEventsStorage = appManagedEventsLocalDataSource,
        premiumBillingDataSource = playBillingDataSource,
        premiumPreferencesStorage = premiumPreferencesLocalDataSource
    )

    val createCalendarEventUseCase: CreateCalendarEventUseCase
        get() = sharedContainer.createCalendarEventUseCase
    val schedulePhoneStateForEventUseCase: SchedulePhoneStateForEventUseCase
        get() = sharedContainer.schedulePhoneStateForEventUseCase
    val saveAppManagedEventUseCase: SaveAppManagedEventUseCase
        get() = sharedContainer.saveAppManagedEventUseCase
    val getAppManagedEventsUseCase: GetAppManagedEventsUseCase
        get() = sharedContainer.getAppManagedEventsUseCase
    val cleanupExpiredManagedEventsUseCase: CleanupExpiredManagedEventsUseCase
        get() = sharedContainer.cleanupExpiredManagedEventsUseCase
    val removeAppManagedEventUseCase: RemoveAppManagedEventUseCase
        get() = sharedContainer.removeAppManagedEventUseCase
    val updateAppManagedEventPhoneStateUseCase: UpdateAppManagedEventPhoneStateUseCase
        get() = sharedContainer.updateAppManagedEventPhoneStateUseCase
    val observePremiumStatusUseCase: ObservePremiumStatusUseCase
        get() = sharedContainer.observePremiumStatusUseCase
    val refreshPremiumStatusUseCase: RefreshPremiumStatusUseCase
        get() = sharedContainer.refreshPremiumStatusUseCase
    val getPremiumPriceUseCase: GetPremiumPriceUseCase
        get() = sharedContainer.getPremiumPriceUseCase
    val canCreateEventThisWeekUseCase: CanCreateEventThisWeekUseCase
        get() = sharedContainer.canCreateEventThisWeekUseCase
    val canEditExistingEventModeUseCase: CanEditExistingEventModeUseCase
        get() = sharedContainer.canEditExistingEventModeUseCase

    fun createMainViewModel() = sharedContainer.createMainViewModel()
}
