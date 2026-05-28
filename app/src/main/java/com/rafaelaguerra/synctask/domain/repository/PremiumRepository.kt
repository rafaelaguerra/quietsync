package com.rafaelaguerra.synctask.domain.repository

import kotlinx.coroutines.flow.Flow

interface PremiumRepository {
    fun observeIsPremium(): Flow<Boolean>

    suspend fun refreshPremiumStatus(): Result<Boolean>

    suspend fun getPremiumPriceLabel(): Result<String>
}
