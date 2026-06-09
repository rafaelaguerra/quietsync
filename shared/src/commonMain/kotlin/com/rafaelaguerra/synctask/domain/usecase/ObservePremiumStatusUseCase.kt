package com.rafaelaguerra.synctask.domain.usecase

import com.rafaelaguerra.synctask.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow

class ObservePremiumStatusUseCase(
    private val repository: PremiumRepository
) {
    operator fun invoke(): Flow<Boolean> = repository.observeIsPremium()
}
