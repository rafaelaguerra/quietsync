package com.rafaelaguerra.synctask.data.local

import android.content.Context
import com.rafaelaguerra.synctask.data.source.PremiumPreferencesStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PremiumPreferencesLocalDataSource(
    context: Context
) : PremiumPreferencesStorage {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val premiumState = MutableStateFlow(
        preferences.getBoolean(KEY_IS_PREMIUM, false)
    )

    override fun observeIsPremium(): StateFlow<Boolean> = premiumState.asStateFlow()

    override fun setIsPremium(isPremium: Boolean) {
        premiumState.value = isPremium
        preferences.edit().putBoolean(KEY_IS_PREMIUM, isPremium).apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "premium_preferences_store"
        private const val KEY_IS_PREMIUM = "key_is_premium"
    }
}
