package com.rafaelaguerra.synctask

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.rafaelaguerra.synctask.data.device.PhoneStateController
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.presentation.SynctaskApp
import com.rafaelaguerra.synctask.presentation.common.UiText
import com.rafaelaguerra.synctask.presentation.common.toUiText
import com.rafaelaguerra.synctask.presentation.main.MainViewModel
import com.rafaelaguerra.synctask.presentation.main.MainViewModelFactory
import com.rafaelaguerra.synctask.presentation.startup.ForceUpdateScreen
import com.rafaelaguerra.synctask.ui.theme.SynctaskTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val appContainer by lazy { (application as SyncTaskApplication).appContainer }

    private val mainViewModel: MainViewModel by viewModels {
        MainViewModelFactory(appContainer)
    }

    private var shouldCreateEventAfterPermission = false
    private var pendingRemoveEventId: Long? = null
    private var showCalendarRationale by mutableStateOf(false)
    private var showDndRationale by mutableStateOf(false)
    private var onboardingPermStep: OnboardingPermStep? = null
    private var swipeHintShownCount by mutableStateOf(0)
    private var showOnboarding by mutableStateOf(false)
    private var startupGateState by mutableStateOf(StartupGateState.Checking)
    private var showFlexibleUpdateDialog by mutableStateOf(false)
    private var pendingInAppUpdateRequest: InAppUpdateRequestType? = null
    private var hasRequestedSoftUpdate = false

    private val preferences by lazy {
        getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
    }

    private val calendarPermissions = arrayOf(
        Manifest.permission.READ_CALENDAR,
        Manifest.permission.WRITE_CALENDAR
    )

    private val inAppUpdateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (pendingInAppUpdateRequest) {
            InAppUpdateRequestType.Immediate -> {
                if (result.resultCode == RESULT_OK) {
                    checkAppVersionGate()
                } else {
                    startupGateState = StartupGateState.UpdateRequired
                }
            }

            InAppUpdateRequestType.Flexible -> Unit
            null -> Unit
        }
        pendingInAppUpdateRequest = null
    }

    private val flexibleInstallStateListener = InstallStateUpdatedListener { installState ->
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {
            showFlexibleUpdateDialog = true
        }
    }

    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result.values.all { it }
        when {
            onboardingPermStep == OnboardingPermStep.Calendar -> {
                // Whether granted or not, advance the onboarding sequence to the next step.
                // The user can always grant permissions later from the in-app prompts.
                onboardingPermStep = OnboardingPermStep.Dnd
                advanceOnboardingPermissions()
            }
            granted && shouldCreateEventAfterPermission -> {
                shouldCreateEventAfterPermission = false
                createEventIfPossible()
            }
            granted && pendingRemoveEventId != null -> {
                val id = pendingRemoveEventId; pendingRemoveEventId = null
                id?.let { mainViewModel.removeAppManagedEvent(it) }
            }
            !granted -> {
                shouldCreateEventAfterPermission = false
                pendingRemoveEventId = null
                mainViewModel.showMessage(UiText.Dynamic(getString(R.string.msg_calendar_permission_required)))
            }
        }
    }
 
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition { startupGateState == StartupGateState.Checking }

        swipeHintShownCount = preferences.getInt(KEY_SWIPE_HINT_SHOWN_COUNT, 0)
        showOnboarding = !preferences.getBoolean(KEY_HAS_SEEN_ONBOARDING, false)
        checkAppVersionGate()

        // Back press: if on create screen, go back to list
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val uiState = mainViewModel.uiState.value
                if (uiState.isCreateEventVisible || uiState.createdEventPreview != null) {
                    mainViewModel.goToEventList()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        setContent {
            SynctaskTheme {
                when (startupGateState) {
                    StartupGateState.Checking -> Unit

                    StartupGateState.UpdateRequired -> {
                        ForceUpdateScreen(
                            onUpdateClick = ::openPlayStoreForUpdate
                        )
                    }

                    StartupGateState.Ready -> {
                        val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()

                        SynctaskApp(
                            uiState = uiState,
                            showOnboarding = showOnboarding,
                            canShowSwipeHint = swipeHintShownCount < MAX_SWIPE_HINT_SHOWS,
                            onSwipeHintShown = ::onSwipeHintShown,
                            onFinishOnboarding = ::finishOnboardingWithPermissions,
                            onSkipOnboarding = ::skipOnboarding,
                            onCreateEventRequested = ::onCreateEventRequested,
                            onCreateEventConfirmed = ::createEventIfPossible,
                            onRemoveEventRequested = ::removeEventIfPossible,
                            onReportIssueRequested = ::onReportIssueRequested,
                            onRefreshRequested = mainViewModel::loadAppManagedEvents,
                            onMessageShown = mainViewModel::clearMessage,
                            onPremiumPurchaseRequested = ::onPremiumPurchaseRequested,
                            onRestorePurchaseRequested = ::onRestorePurchaseRequested,
                            viewModel = mainViewModel
                        )

                        if (showCalendarRationale) {
                            PermissionRationaleDialog(
                                title = stringResource(R.string.perm_dialog_calendar_title),
                                body = stringResource(R.string.perm_dialog_calendar_body),
                                confirmText = stringResource(R.string.perm_dialog_continue),
                                onConfirm = {
                                    showCalendarRationale = false
                                    calendarPermissionLauncher.launch(calendarPermissions)
                                },
                                onDismiss = {
                                    showCalendarRationale = false
                                    shouldCreateEventAfterPermission = false
                                    pendingRemoveEventId = null
                                }
                            )
                        }

                        if (showDndRationale) {
                            PermissionRationaleDialog(
                                title = stringResource(R.string.perm_dialog_dnd_title),
                                body = stringResource(R.string.perm_dialog_dnd_body),
                                confirmText = stringResource(R.string.perm_dialog_open_settings),
                                onConfirm = {
                                    showDndRationale = false
                                    openDndSettings()
                                    advanceOnboardingAfterDndDialog()
                                },
                                onDismiss = {
                                    showDndRationale = false
                                    advanceOnboardingAfterDndDialog()
                                }
                            )
                        }

                        if (showFlexibleUpdateDialog) {
                            AlertDialog(
                                onDismissRequest = { showFlexibleUpdateDialog = false },
                                title = { Text(text = stringResource(R.string.soft_update_title)) },
                                text = { Text(text = stringResource(R.string.soft_update_subtitle)) },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            showFlexibleUpdateDialog = false
                                            appContainer.inAppUpdateCoordinator.completeFlexibleUpdate()
                                        }
                                    ) {
                                        Text(text = stringResource(R.string.soft_update_cta_restart))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showFlexibleUpdateDialog = false }) {
                                        Text(text = stringResource(R.string.soft_update_cta_later))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        appContainer.inAppUpdateCoordinator.registerInstallStateListener(flexibleInstallStateListener)
    }

    override fun onResume() {
        super.onResume()
        appContainer.inAppUpdateCoordinator.checkIfFlexibleUpdateDownloaded {
            showFlexibleUpdateDialog = true
        }
    }

    override fun onStop() {
        appContainer.inAppUpdateCoordinator.unregisterInstallStateListener(flexibleInstallStateListener)
        super.onStop()
    }

    override fun onDestroy() {
        appContainer.playBillingDataSource.destroy()
        super.onDestroy()
    }

    private fun onCreateEventRequested() {
        mainViewModel.onCreateEventEntryRequested()
    }

    private fun checkAppVersionGate() {
        appContainer.appVersionGatekeeper.checkVersion { result ->
            if (result.isHardUpdateRequired) {
                startupGateState = StartupGateState.Checking
                startImmediateUpdateFlow()
                return@checkVersion
            }

            startupGateState = StartupGateState.Ready
            if (result.isSoftUpdateRecommended) {
                maybeStartFlexibleUpdate()
            }
        }
    }

    private fun startImmediateUpdateFlow() {
        pendingInAppUpdateRequest = InAppUpdateRequestType.Immediate
        appContainer.inAppUpdateCoordinator.startImmediateUpdateIfAvailable(
            launcher = inAppUpdateLauncher
        ) { started ->
            if (!started) {
                pendingInAppUpdateRequest = null
                startupGateState = StartupGateState.UpdateRequired
            }
        }
    }

    private fun maybeStartFlexibleUpdate() {
        if (hasRequestedSoftUpdate) return
        hasRequestedSoftUpdate = true
        pendingInAppUpdateRequest = InAppUpdateRequestType.Flexible
        appContainer.inAppUpdateCoordinator.startFlexibleUpdateIfAvailable(
            launcher = inAppUpdateLauncher
        ) { started ->
            if (!started) {
                pendingInAppUpdateRequest = null
            }
        }
    }

    private fun skipOnboarding() {
        preferences.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, true).apply()
        onboardingPermStep = null
        showOnboarding = false
    }

    /**
     * Triggered when the user taps "Empezar" on the last onboarding page. We mark the
     * onboarding as seen immediately and then walk a small permission state machine:
     * Calendar → Do Not Disturb → Done. The flow is best-effort: even if the user denies
     * a step, we still complete onboarding so they aren't stuck.
     */
    private fun finishOnboardingWithPermissions() {
        preferences.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, true).apply()
        onboardingPermStep = OnboardingPermStep.Calendar
        advanceOnboardingPermissions()
    }

    private fun advanceOnboardingPermissions() {
        when (onboardingPermStep) {
            OnboardingPermStep.Calendar -> {
                if (hasCalendarPermissions()) {
                    onboardingPermStep = OnboardingPermStep.Dnd
                    advanceOnboardingPermissions()
                } else {
                    // Result handler will advance to the next step.
                    calendarPermissionLauncher.launch(calendarPermissions)
                }
            }
            OnboardingPermStep.Dnd -> {
                if (hasDoNotDisturbAccess()) {
                    onboardingPermStep = OnboardingPermStep.Done
                    advanceOnboardingPermissions()
                } else {
                    // Dialog dismissal will advance the step via advanceOnboardingAfterDndDialog().
                    showDndRationale = true
                }
            }
            OnboardingPermStep.Done -> {
                onboardingPermStep = null
                showOnboarding = false
            }
            null -> Unit
        }
    }

    private fun advanceOnboardingAfterDndDialog() {
        if (onboardingPermStep != OnboardingPermStep.Dnd) return
        onboardingPermStep = OnboardingPermStep.Done
        advanceOnboardingPermissions()
    }

    private fun onReportIssueRequested() {
        val subject = Uri.encode(getString(R.string.email_issue_subject))
        val body = Uri.encode(getString(R.string.email_issue_body))
        val emailUri = Uri.parse("mailto:$SUPPORT_EMAIL?subject=$subject&body=$body")
        val intent = Intent(Intent.ACTION_SENDTO, emailUri)
        runCatching { startActivity(intent) }
            .onFailure {
                mainViewModel.showMessage(UiText.Dynamic(getString(R.string.msg_email_app_unavailable)))
            }
    }

    private fun onPremiumPurchaseRequested() {
        mainViewModel.setPurchaseInProgress(true)
        appContainer.playBillingDataSource.launchPremiumPurchase(this) { result ->
            result
                .onSuccess { unlocked ->
                    mainViewModel.setPurchaseInProgress(false)
                    mainViewModel.refreshPremiumStatus(showErrors = true)
                    if (unlocked) {
                        mainViewModel.hidePaywall()
                        mainViewModel.showMessage(UiText.Dynamic(getString(R.string.msg_premium_activated)))
                    } else {
                        mainViewModel.showMessage(UiText.Dynamic(getString(R.string.msg_purchase_pending)))
                    }
                }
                .onFailure { error ->
                    appContainer.errorTracker.recordError(error, "Premium purchase failed")
                    mainViewModel.setPurchaseInProgress(false)
                    mainViewModel.showMessage(error.toUiText())
                }
        }
    }

    private fun onRestorePurchaseRequested() {
        mainViewModel.setPurchaseInProgress(true)
        lifecycleScope.launch {
            mainViewModel.refreshPremiumStatus(showErrors = true)
            mainViewModel.setPurchaseInProgress(false)
            if (mainViewModel.uiState.value.isPremium) {
                mainViewModel.hidePaywall()
                mainViewModel.showMessage(UiText.Dynamic(getString(R.string.msg_restore_success)))
            } else {
                mainViewModel.showMessage(UiText.Dynamic(getString(R.string.msg_restore_not_found)))
            }
        }
    }

    private fun createEventIfPossible() {
        if (!hasCalendarPermissions()) {
            shouldCreateEventAfterPermission = true
            pendingRemoveEventId = null
            // Show the rationale before launching the system prompt so the user understands why.
            showCalendarRationale = true
            return
        }

        if (mainViewModel.uiState.value.selectedPhoneState == PhoneState.DO_NOT_DISTURB &&
            !hasDoNotDisturbAccess()
        ) {
            // Always show the rationale: it makes the multi-step Android Settings flow clearer.
            showDndRationale = true
            return
        }

        if (mainViewModel.uiState.value.selectedPhoneState == PhoneState.AIRPLANE_MODE) {
            val phoneStateController = PhoneStateController(this)
            if (!phoneStateController.canUseAirplaneModeAutomation()) {
                mainViewModel.showMessage(UiText.Dynamic(getString(R.string.msg_airplane_mode_manual)))
                openAirplaneModeSettings()
                return
            }
        }

        mainViewModel.createEventAndScheduleState()
    }

    private fun removeEventIfPossible(eventId: Long) {
        if (!hasCalendarPermissions()) {
            pendingRemoveEventId = eventId
            shouldCreateEventAfterPermission = false
            showCalendarRationale = true
            return
        }
        mainViewModel.removeAppManagedEvent(eventId)
    }

    private fun onSwipeHintShown() {
        if (swipeHintShownCount >= MAX_SWIPE_HINT_SHOWS) return
        swipeHintShownCount += 1
        preferences.edit().putInt(KEY_SWIPE_HINT_SHOWN_COUNT, swipeHintShownCount).apply()
    }

    private fun openPlayStoreForUpdate() {
        val marketIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        ).apply {
            setPackage("com.android.vending")
        }
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )

        val opened = runCatching { startActivity(marketIntent) }.isSuccess ||
            runCatching { startActivity(webIntent) }.isSuccess

        if (!opened) {
            Toast.makeText(this, getString(R.string.msg_play_store_unavailable), Toast.LENGTH_LONG).show()
        }
    }

    private fun openDndSettings() {
        // On Android 11+ we can deep-link straight to QuietSync's DND access page,
        // skipping the long list of apps the user would otherwise have to scroll.
        // The detail action constant is @hide in the framework, but the action string
        // is stable and resolved by the Settings app from API 30 onwards.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val detailIntent = Intent("android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS")
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            if (detailIntent.resolveActivity(packageManager) != null) {
                startActivity(detailIntent)
                return
            }
        }
        startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }

    private fun openAirplaneModeSettings() {
        startActivity(Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS))
    }

    private fun hasCalendarPermissions(): Boolean =
        calendarPermissions.all { p ->
            ContextCompat.checkSelfPermission(this, p) == PermissionChecker.PERMISSION_GRANTED
        }

    private fun hasDoNotDisturbAccess(): Boolean =
        getSystemService(NotificationManager::class.java)?.isNotificationPolicyAccessGranted == true

    companion object {
        private const val PREFERENCES_NAME = "sync_task_preferences"
        private const val KEY_HAS_SEEN_ONBOARDING = "key_has_seen_onboarding"
        private const val KEY_SWIPE_HINT_SHOWN_COUNT = "key_swipe_hint_shown_count"
        private const val MAX_SWIPE_HINT_SHOWS = 2
        private const val SUPPORT_EMAIL = "support@quietsync.app"
    }
}

private enum class StartupGateState {
    Checking,
    Ready,
    UpdateRequired
}

private enum class InAppUpdateRequestType {
    Immediate,
    Flexible
}

private enum class OnboardingPermStep {
    Calendar,
    Dnd,
    Done
}

@Composable
private fun PermissionRationaleDialog(
    title: String,
    body: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = androidx.compose.ui.res.stringResource(R.string.perm_dialog_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
