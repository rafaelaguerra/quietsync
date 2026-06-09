package com.rafaelaguerra.synctask.domain.usecase

class CanEditExistingEventModeUseCase {
    operator fun invoke(isPremium: Boolean): Boolean = isPremium
}
