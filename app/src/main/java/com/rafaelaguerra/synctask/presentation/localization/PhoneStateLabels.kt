package com.rafaelaguerra.synctask.presentation.localization

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rafaelaguerra.synctask.R
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.model.RepeatPeriod

@StringRes
fun PhoneState.labelRes(): Int = when (this) {
    PhoneState.NORMAL -> R.string.phone_state_normal
    PhoneState.VIBRATE -> R.string.phone_state_vibrate
    PhoneState.SILENT -> R.string.phone_state_silent
    PhoneState.DO_NOT_DISTURB -> R.string.phone_state_dnd
    PhoneState.AIRPLANE_MODE -> R.string.phone_state_airplane
}

@Composable
fun PhoneState.displayLabel(): String = stringResource(labelRes())

fun PhoneState.displayLabel(context: Context): String = context.getString(labelRes())

@Composable
fun RepeatPeriod.displayLabel(): String = stringResource(
    when (this) {
        RepeatPeriod.DAILY -> R.string.repeat_daily
        RepeatPeriod.WEEKLY -> R.string.repeat_weekly
        RepeatPeriod.MONTHLY -> R.string.repeat_monthly
    }
)
