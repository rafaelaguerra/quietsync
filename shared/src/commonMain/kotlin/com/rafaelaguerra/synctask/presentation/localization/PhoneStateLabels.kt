package com.rafaelaguerra.synctask.presentation.localization

import androidx.compose.runtime.Composable
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.domain.model.RepeatPeriod
import com.rafaelaguerra.synctask.resources.Res
import com.rafaelaguerra.synctask.resources.phone_state_airplane
import com.rafaelaguerra.synctask.resources.phone_state_dnd
import com.rafaelaguerra.synctask.resources.phone_state_normal
import com.rafaelaguerra.synctask.resources.phone_state_silent
import com.rafaelaguerra.synctask.resources.phone_state_vibrate
import com.rafaelaguerra.synctask.resources.repeat_daily
import com.rafaelaguerra.synctask.resources.repeat_monthly
import com.rafaelaguerra.synctask.resources.repeat_weekly
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

fun PhoneState.labelRes(): StringResource = when (this) {
    PhoneState.NORMAL -> Res.string.phone_state_normal
    PhoneState.VIBRATE -> Res.string.phone_state_vibrate
    PhoneState.SILENT -> Res.string.phone_state_silent
    PhoneState.DO_NOT_DISTURB -> Res.string.phone_state_dnd
    PhoneState.AIRPLANE_MODE -> Res.string.phone_state_airplane
}

@Composable
fun PhoneState.displayLabel(): String = stringResource(labelRes())

@Composable
fun RepeatPeriod.displayLabel(): String = stringResource(
    when (this) {
        RepeatPeriod.DAILY -> Res.string.repeat_daily
        RepeatPeriod.WEEKLY -> Res.string.repeat_weekly
        RepeatPeriod.MONTHLY -> Res.string.repeat_monthly
    }
)
