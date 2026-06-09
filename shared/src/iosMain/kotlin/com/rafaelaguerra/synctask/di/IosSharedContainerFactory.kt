package com.rafaelaguerra.synctask.di

import com.rafaelaguerra.synctask.data.billing.IosPremiumBillingDataSource
import com.rafaelaguerra.synctask.data.calendar.IosCalendarDataSource
import com.rafaelaguerra.synctask.data.device.NoOpPhoneStateScheduler
import com.rafaelaguerra.synctask.data.local.IosAppManagedEventsStorage
import com.rafaelaguerra.synctask.data.local.IosPremiumPreferencesStorage

private var billingDataSource: IosPremiumBillingDataSource? = null

fun createSharedContainer(): SharedContainer {
    val premiumPreferences = IosPremiumPreferencesStorage()
    val billing = IosPremiumBillingDataSource(premiumPreferences).also {
        billingDataSource = it
    }
    return SharedContainer(
        calendarDataSource = IosCalendarDataSource(),
        phoneStateScheduler = NoOpPhoneStateScheduler(),
        appManagedEventsStorage = IosAppManagedEventsStorage(),
        premiumBillingDataSource = billing,
        premiumPreferencesStorage = premiumPreferences
    )
}

/** StoreKit billing instance for purchase/restore flows from the iOS host UI. */
fun iosPremiumBillingDataSource(): IosPremiumBillingDataSource =
    billingDataSource ?: error("SharedContainer not initialized")
