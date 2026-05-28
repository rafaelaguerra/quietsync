package com.rafaelaguerra.synctask.data.update

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateCoordinator(
    context: Context,
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
) {

    fun startImmediateUpdateIfAvailable(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onLaunchResult: (Boolean) -> Unit
    ) {
        launchIfAvailable(
            updateType = AppUpdateType.IMMEDIATE,
            launcher = launcher,
            allowImmediateInProgress = true,
            onLaunchResult = onLaunchResult
        )
    }

    fun startFlexibleUpdateIfAvailable(
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        onLaunchResult: (Boolean) -> Unit
    ) {
        launchIfAvailable(
            updateType = AppUpdateType.FLEXIBLE,
            launcher = launcher,
            allowImmediateInProgress = false,
            onLaunchResult = onLaunchResult
        )
    }

    fun checkIfFlexibleUpdateDownloaded(onDownloaded: () -> Unit) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    onDownloaded()
                }
            }
    }

    fun completeFlexibleUpdate() {
        appUpdateManager.completeUpdate()
    }

    fun registerInstallStateListener(listener: InstallStateUpdatedListener) {
        appUpdateManager.registerListener(listener)
    }

    fun unregisterInstallStateListener(listener: InstallStateUpdatedListener) {
        appUpdateManager.unregisterListener(listener)
    }

    private fun launchIfAvailable(
        updateType: Int,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
        allowImmediateInProgress: Boolean,
        onLaunchResult: (Boolean) -> Unit
    ) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                val isUpdateAvailable = appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(updateType)
                val isImmediateInProgress = allowImmediateInProgress &&
                    appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

                if (!isUpdateAvailable && !isImmediateInProgress) {
                    onLaunchResult(false)
                    return@addOnSuccessListener
                }

                val startResult = runCatching {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        launcher,
                        AppUpdateOptions.newBuilder(updateType).build()
                    )
                }.getOrDefault(false)

                onLaunchResult(startResult)
            }
            .addOnFailureListener {
                onLaunchResult(false)
            }
    }
}
