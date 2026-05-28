package com.rafaelaguerra.synctask.data.remote

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.rafaelaguerra.synctask.R

class AppVersionGatekeeper(
    private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
) {

    fun checkVersion(onResult: (VersionGateResult) -> Unit) {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setFetchTimeoutInSeconds(FETCH_TIMEOUT_SECONDS)
            .setMinimumFetchIntervalInSeconds(
                if (isDebugBuild()) DEBUG_MIN_FETCH_INTERVAL_SECONDS else PROD_MIN_FETCH_INTERVAL_SECONDS
            )
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)
            .continueWithTask { remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults) }
            .continueWithTask { remoteConfig.fetchAndActivate() }
            .addOnCompleteListener {
                onResult(resolveVersionGateResult())
            }
    }

    private fun resolveVersionGateResult(): VersionGateResult {
        val minSupportedVersionCode = remoteConfig.getLong(MIN_SUPPORTED_VERSION_CODE_KEY)
        val softUpdateMinVersionCode = remoteConfig.getLong(SOFT_UPDATE_MIN_VERSION_CODE_KEY)
        val currentVersionCode = getCurrentVersionCode()
        val isHardUpdateRequired = currentVersionCode < minSupportedVersionCode

        return VersionGateResult(
            isHardUpdateRequired = isHardUpdateRequired,
            isSoftUpdateRecommended = !isHardUpdateRequired && currentVersionCode < softUpdateMinVersionCode,
            minSupportedVersionCode = minSupportedVersionCode,
            softUpdateMinVersionCode = softUpdateMinVersionCode
        )
    }

    @Suppress("DEPRECATION")
    private fun getCurrentVersionCode(): Long {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

        return PackageInfoCompat.getLongVersionCode(packageInfo)
    }

    private fun isDebugBuild(): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    companion object {
        const val MIN_SUPPORTED_VERSION_CODE_KEY = "min_supported_version_code"
        const val SOFT_UPDATE_MIN_VERSION_CODE_KEY = "soft_update_min_version_code"

        private const val FETCH_TIMEOUT_SECONDS = 10L
        private const val DEBUG_MIN_FETCH_INTERVAL_SECONDS = 0L
        private const val PROD_MIN_FETCH_INTERVAL_SECONDS = 60L * 60L
    }
}

data class VersionGateResult(
    val isHardUpdateRequired: Boolean,
    val isSoftUpdateRecommended: Boolean,
    val minSupportedVersionCode: Long,
    val softUpdateMinVersionCode: Long
)
