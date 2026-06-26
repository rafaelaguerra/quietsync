package com.rafaelaguerra.synctask.presentation.main

import com.rafaelaguerra.synctask.di.SharedContainer

fun SharedContainer.createMainViewModel(): MainViewModel = MainViewModel(
    createCalendarEventUseCase = createCalendarEventUseCase,
    schedulePhoneStateForEventUseCase = schedulePhoneStateForEventUseCase,
    saveAppManagedEventUseCase = saveAppManagedEventUseCase,
    getAppManagedEventsUseCase = getAppManagedEventsUseCase,
    cleanupExpiredManagedEventsUseCase = cleanupExpiredManagedEventsUseCase,
    removeAppManagedEventUseCase = removeAppManagedEventUseCase,
    updateAppManagedEventPhoneStateUseCase = updateAppManagedEventPhoneStateUseCase,
    observePremiumStatusUseCase = observePremiumStatusUseCase,
    refreshPremiumStatusUseCase = refreshPremiumStatusUseCase,
    getPremiumPriceUseCase = getPremiumPriceUseCase,
    canCreateEventThisWeekUseCase = canCreateEventThisWeekUseCase,
    canEditExistingEventModeUseCase = canEditExistingEventModeUseCase,
    errorTracker = errorTracker
)
