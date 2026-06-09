package com.rafaelaguerra.synctask.presentation

import androidx.compose.runtime.Composable
import com.rafaelaguerra.synctask.presentation.main.CreateEventScreen
import com.rafaelaguerra.synctask.presentation.main.EventSuccessScreen
import com.rafaelaguerra.synctask.presentation.main.MainScreen
import com.rafaelaguerra.synctask.presentation.main.MainUiState
import com.rafaelaguerra.synctask.presentation.main.MainViewModel
import com.rafaelaguerra.synctask.presentation.main.PremiumPaywallScreen
import com.rafaelaguerra.synctask.presentation.onboarding.OnboardingPermissionMode
import com.rafaelaguerra.synctask.presentation.onboarding.OnboardingScreen

/**
 * Shared navigation shell for Android and iOS. Platform hosts supply permission,
 * billing, and startup-gate behavior via the callback parameters.
 */
@Composable
fun SynctaskApp(
    uiState: MainUiState,
    showOnboarding: Boolean,
    canShowSwipeHint: Boolean,
    onSwipeHintShown: () -> Unit,
    onFinishOnboarding: () -> Unit,
    onSkipOnboarding: () -> Unit,
    onCreateEventRequested: () -> Unit,
    onCreateEventConfirmed: () -> Unit,
    onRemoveEventRequested: (Long) -> Unit,
    onReportIssueRequested: () -> Unit,
    onRefreshRequested: () -> Unit,
    onMessageShown: () -> Unit,
    onPremiumPurchaseRequested: () -> Unit,
    onRestorePurchaseRequested: () -> Unit,
    onboardingPermissionMode: OnboardingPermissionMode = OnboardingPermissionMode.FULL,
    viewModel: MainViewModel
) {
    val createdEventPreview = uiState.createdEventPreview

    when {
        showOnboarding -> {
            OnboardingScreen(
                onFinish = onFinishOnboarding,
                onSkip = onSkipOnboarding,
                permissionMode = onboardingPermissionMode
            )
        }

        uiState.showPaywall -> {
            PremiumPaywallScreen(
                reason = uiState.paywallReason,
                premiumPriceLabel = uiState.premiumPriceLabel,
                isPurchaseInProgress = uiState.isPurchaseInProgress,
                onPurchaseRequested = onPremiumPurchaseRequested,
                onRestoreRequested = onRestorePurchaseRequested,
                onCloseRequested = viewModel::hidePaywall
            )
        }

        createdEventPreview != null -> {
            EventSuccessScreen(
                preview = createdEventPreview,
                onGoToList = viewModel::goToEventList,
                onCreateAnotherEvent = viewModel::onCreateAnotherEvent
            )
        }

        uiState.isCreateEventVisible -> {
            CreateEventScreen(
                uiState = uiState,
                onBack = viewModel::hideCreateEvent,
                onTitleChange = viewModel::onTitleChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onLocationChange = viewModel::onLocationChange,
                onStartDateTimeChange = viewModel::onStartDateTimeChange,
                onEndDateTimeChange = viewModel::onEndDateTimeChange,
                onPhoneStateSelected = viewModel::onPhoneStateSelected,
                onRecurringToggled = viewModel::onRecurringToggled,
                onRecurrenceDayToggled = viewModel::onRecurrenceDayToggled,
                onRecurrencePeriodSelected = viewModel::onRecurrencePeriodSelected,
                onCreateEventRequested = onCreateEventConfirmed
            )
        }

        else -> {
            MainScreen(
                uiState = uiState,
                canShowSwipeHint = canShowSwipeHint,
                onSwipeHintShown = onSwipeHintShown,
                onRemoveEventRequested = onRemoveEventRequested,
                onFilterSelected = viewModel::onFilterSelected,
                onCreateEventRequested = onCreateEventRequested,
                onReportIssueRequested = onReportIssueRequested,
                onRefreshRequested = onRefreshRequested,
                onMessageShown = onMessageShown
            )
        }
    }
}
