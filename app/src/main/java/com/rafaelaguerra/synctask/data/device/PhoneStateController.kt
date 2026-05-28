package com.rafaelaguerra.synctask.data.device

import android.app.NotificationManager
import android.content.pm.PackageManager
import android.content.Context
import android.media.AudioManager
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.rafaelaguerra.synctask.domain.error.AppError
import com.rafaelaguerra.synctask.domain.error.asException
import com.rafaelaguerra.synctask.domain.model.PhoneState

class PhoneStateController(
    context: Context
) {
    private val appContext = context.applicationContext
    private val audioManager = appContext.getSystemService(AudioManager::class.java)
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)

    fun canUseDoNotDisturb(): Boolean {
        return notificationManager?.isNotificationPolicyAccessGranted == true
    }

    /**
     * Airplane mode automation is restricted on modern Android for regular apps.
     * We expose this check to avoid misleading UX and gate unsupported flows.
     */
    fun canUseAirplaneModeAutomation(): Boolean {
        val hasWriteSecureSettings = ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.WRITE_SECURE_SETTINGS
        ) == PermissionChecker.PERMISSION_GRANTED
        return hasWriteSecureSettings &&
            appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    fun getCurrentState(): PhoneState {
        val interruptionFilter = notificationManager?.currentInterruptionFilter
        if (interruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE) {
            return PhoneState.DO_NOT_DISTURB
        }

        return when (audioManager?.ringerMode) {
            AudioManager.RINGER_MODE_VIBRATE -> PhoneState.VIBRATE
            AudioManager.RINGER_MODE_SILENT -> PhoneState.SILENT
            else -> PhoneState.NORMAL
        }
    }

    fun applyState(phoneState: PhoneState) {
        val safeAudioManager = audioManager ?: return
        val safeNotificationManager = notificationManager

        when (phoneState) {
            PhoneState.NORMAL -> {
                if (safeNotificationManager?.isNotificationPolicyAccessGranted == true) {
                    safeNotificationManager.setInterruptionFilter(
                        NotificationManager.INTERRUPTION_FILTER_ALL
                    )
                }
                safeAudioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }

            PhoneState.VIBRATE -> {
                if (safeNotificationManager?.isNotificationPolicyAccessGranted == true) {
                    safeNotificationManager.setInterruptionFilter(
                        NotificationManager.INTERRUPTION_FILTER_ALL
                    )
                }
                safeAudioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
            }

            PhoneState.SILENT -> {
                if (safeNotificationManager?.isNotificationPolicyAccessGranted == true) {
                    safeNotificationManager.setInterruptionFilter(
                        NotificationManager.INTERRUPTION_FILTER_ALL
                    )
                }
                safeAudioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
            }

            PhoneState.DO_NOT_DISTURB -> {
                if (safeNotificationManager?.isNotificationPolicyAccessGranted != true) {
                    throw AppError.DoNotDisturbAccessDenied.asException()
                }
                safeNotificationManager.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_NONE
                )
            }

            PhoneState.AIRPLANE_MODE -> {
                throw AppError.AirplaneModeNotAutomatable.asException()
            }
        }
    }
}
