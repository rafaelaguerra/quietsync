package com.rafaelaguerra.synctask.data.source

import kotlinx.coroutines.flow.Flow

interface PremiumPreferencesStorage {
    fun observeIsPremium(): Flow<Boolean>
    fun setIsPremium(isPremium: Boolean)
}
