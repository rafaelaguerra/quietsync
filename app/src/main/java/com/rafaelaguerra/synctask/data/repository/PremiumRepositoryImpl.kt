package com.rafaelaguerra.synctask.data.repository

import com.rafaelaguerra.synctask.data.billing.PlayBillingDataSource
import com.rafaelaguerra.synctask.data.local.PremiumPreferencesLocalDataSource
import com.rafaelaguerra.synctask.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow

class PremiumRepositoryImpl(
    private val playBillingDataSource: PlayBillingDataSource,
    private val premiumPreferencesLocalDataSource: PremiumPreferencesLocalDataSource
) : PremiumRepository {

    override fun observeIsPremium(): Flow<Boolean> {
        return premiumPreferencesLocalDataSource.observeIsPremium()
    }

    override suspend fun refreshPremiumStatus(): Result<Boolean> {
        return playBillingDataSource.refreshPremiumEntitlement()
            .onSuccess { premiumPreferencesLocalDataSource.setIsPremium(it) }
    }

    override suspend fun getPremiumPriceLabel(): Result<String> {
        return playBillingDataSource.queryPremiumPriceLabel()
    }
}
