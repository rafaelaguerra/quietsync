package com.rafaelaguerra.synctask.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rafaelaguerra.synctask.di.createSharedContainer
import com.rafaelaguerra.synctask.di.iosPremiumBillingDataSource
import com.rafaelaguerra.synctask.presentation.common.UiText
import com.rafaelaguerra.synctask.presentation.common.toUiText
import com.rafaelaguerra.synctask.presentation.main.createMainViewModel
import com.rafaelaguerra.synctask.platform.IosAppPreferences
import com.rafaelaguerra.synctask.platform.IosCalendarAccess
import com.rafaelaguerra.synctask.platform.IosUrlOpener
import com.rafaelaguerra.synctask.resources.Res
import com.rafaelaguerra.synctask.resources.email_issue_body
import com.rafaelaguerra.synctask.resources.email_issue_subject
import com.rafaelaguerra.synctask.resources.msg_calendar_permission_required
import com.rafaelaguerra.synctask.resources.msg_email_app_unavailable
import com.rafaelaguerra.synctask.resources.msg_premium_activated
import com.rafaelaguerra.synctask.resources.msg_purchase_pending
import com.rafaelaguerra.synctask.resources.msg_restore_not_found
import com.rafaelaguerra.synctask.resources.msg_restore_success
import com.rafaelaguerra.synctask.presentation.onboarding.OnboardingPermissionMode
import com.rafaelaguerra.synctask.ui.theme.SynctaskTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import platform.UIKit.UIViewController

private const val MAX_SWIPE_HINT_SHOWS = 2
private const val SUPPORT_EMAIL = "support@quietsync.app"

fun MainViewController(): UIViewController {
    val container = createSharedContainer()
    return ComposeUIViewController {
        SynctaskIosHost(container = container)
    }
}

@Composable
fun SynctaskIosHost(container: com.rafaelaguerra.synctask.di.SharedContainer) {
    SynctaskTheme {
        val viewModel = viewModel { container.createMainViewModel() }
        val uiState by viewModel.uiState.collectAsState()
        val scope = rememberCoroutineScope()
        val billing = remember { iosPremiumBillingDataSource() }

        var showOnboarding by remember { mutableStateOf(!IosAppPreferences.hasSeenOnboarding()) }
        var swipeHintShownCount by remember { mutableStateOf(IosAppPreferences.swipeHintShownCount()) }

        LaunchedEffect(Unit) {
            billing.queryPremiumPriceLabel()
            viewModel.refreshPremiumStatus(showErrors = false)
        }

        SynctaskApp(
            uiState = uiState,
            showOnboarding = showOnboarding,
            onboardingPermissionMode = OnboardingPermissionMode.CALENDAR_ONLY,
            canShowSwipeHint = swipeHintShownCount < MAX_SWIPE_HINT_SHOWS,
            onSwipeHintShown = {
                if (swipeHintShownCount < MAX_SWIPE_HINT_SHOWS) {
                    swipeHintShownCount = IosAppPreferences.incrementSwipeHintShownCount()
                }
            },
            onFinishOnboarding = {
                IosAppPreferences.setOnboardingSeen()
                scope.launch {
                    IosCalendarAccess.ensureGranted()
                    showOnboarding = false
                }
            },
            onSkipOnboarding = {
                IosAppPreferences.setOnboardingSeen()
                showOnboarding = false
            },
            onCreateEventRequested = viewModel::onCreateEventEntryRequested,
            onCreateEventConfirmed = {
                scope.launch {
                    if (IosCalendarAccess.ensureGranted()) {
                        viewModel.createEventAndScheduleState()
                    } else {
                        viewModel.showMessage(
                            UiText.Resource(Res.string.msg_calendar_permission_required)
                        )
                    }
                }
            },
            onRemoveEventRequested = { eventId ->
                scope.launch {
                    if (IosCalendarAccess.ensureGranted()) {
                        viewModel.removeAppManagedEvent(eventId)
                    } else {
                        viewModel.showMessage(
                            UiText.Resource(Res.string.msg_calendar_permission_required)
                        )
                    }
                }
            },
            onReportIssueRequested = {
                scope.launch {
                    val subject = getString(Res.string.email_issue_subject)
                    val body = getString(Res.string.email_issue_body)
                    val mailto = IosUrlOpener.buildMailtoUrl(
                        email = SUPPORT_EMAIL,
                        subject = subject,
                        body = body
                    )
                    if (!IosUrlOpener.open(mailto)) {
                        viewModel.showMessage(UiText.Resource(Res.string.msg_email_app_unavailable))
                    }
                }
            },
            onRefreshRequested = viewModel::loadAppManagedEvents,
            onMessageShown = viewModel::clearMessage,
            onPremiumPurchaseRequested = {
                viewModel.setPurchaseInProgress(true)
                scope.launch {
                    billing.purchasePremium()
                        .onSuccess { unlocked ->
                            viewModel.setPurchaseInProgress(false)
                            viewModel.refreshPremiumStatus(showErrors = true)
                            if (unlocked) {
                                viewModel.hidePaywall()
                                viewModel.showMessage(UiText.Resource(Res.string.msg_premium_activated))
                            } else {
                                viewModel.showMessage(UiText.Resource(Res.string.msg_purchase_pending))
                            }
                        }
                        .onFailure { error ->
                            viewModel.setPurchaseInProgress(false)
                            viewModel.showMessage(error.toUiText())
                        }
                }
            },
            onRestorePurchaseRequested = {
                viewModel.setPurchaseInProgress(true)
                scope.launch {
                    billing.restorePurchases()
                        .onSuccess { restored ->
                            viewModel.refreshPremiumStatus(showErrors = true)
                            viewModel.setPurchaseInProgress(false)
                            if (viewModel.uiState.value.isPremium || restored) {
                                viewModel.hidePaywall()
                                viewModel.showMessage(UiText.Resource(Res.string.msg_restore_success))
                            } else {
                                viewModel.showMessage(UiText.Resource(Res.string.msg_restore_not_found))
                            }
                        }
                        .onFailure { error ->
                            viewModel.setPurchaseInProgress(false)
                            viewModel.showMessage(error.toUiText())
                        }
                }
            },
            viewModel = viewModel
        )
    }
}
