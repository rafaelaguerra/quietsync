package com.rafaelaguerra.synctask.data.local

import com.rafaelaguerra.synctask.data.source.PremiumPreferencesStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

class IosPremiumPreferencesStorage : PremiumPreferencesStorage {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val premiumState = MutableStateFlow(defaults.boolForKey(KEY_IS_PREMIUM))

    override fun observeIsPremium(): Flow<Boolean> = premiumState.asStateFlow()

    override fun setIsPremium(isPremium: Boolean) {
        premiumState.value = isPremium
        defaults.setBool(isPremium, KEY_IS_PREMIUM)
    }

    fun currentIsPremium(): Boolean = defaults.boolForKey(KEY_IS_PREMIUM)

    private companion object {
        const val KEY_IS_PREMIUM = "key_is_premium"
    }
}
