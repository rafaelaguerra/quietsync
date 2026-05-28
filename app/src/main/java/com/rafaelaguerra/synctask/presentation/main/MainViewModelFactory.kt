package com.rafaelaguerra.synctask.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rafaelaguerra.synctask.di.AppContainer

class MainViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                createCalendarEventUseCase = appContainer.createCalendarEventUseCase,
                schedulePhoneStateForEventUseCase = appContainer.schedulePhoneStateForEventUseCase,
                saveAppManagedEventUseCase = appContainer.saveAppManagedEventUseCase,
                getAppManagedEventsUseCase = appContainer.getAppManagedEventsUseCase,
                cleanupExpiredManagedEventsUseCase = appContainer.cleanupExpiredManagedEventsUseCase,
                removeAppManagedEventUseCase = appContainer.removeAppManagedEventUseCase,
                updateAppManagedEventPhoneStateUseCase = appContainer.updateAppManagedEventPhoneStateUseCase,
                observePremiumStatusUseCase = appContainer.observePremiumStatusUseCase,
                refreshPremiumStatusUseCase = appContainer.refreshPremiumStatusUseCase,
                getPremiumPriceUseCase = appContainer.getPremiumPriceUseCase,
                canCreateEventThisWeekUseCase = appContainer.canCreateEventThisWeekUseCase,
                canEditExistingEventModeUseCase = appContainer.canEditExistingEventModeUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
