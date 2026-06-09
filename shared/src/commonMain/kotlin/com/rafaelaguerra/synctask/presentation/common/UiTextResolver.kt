package com.rafaelaguerra.synctask.presentation.common

import androidx.compose.runtime.Composable
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.presentation.localization.displayLabel
import com.rafaelaguerra.synctask.presentation.localization.labelRes
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> {
        val labels = phoneStateLabels()
        val resolvedArgs = args.map { arg ->
            if (arg is PhoneState) labels.getValue(arg) else arg
        }
        stringResource(resource, *resolvedArgs.toTypedArray())
    }
}

/**
 * Resolves every [PhoneState] label up-front so the per-arg mapping above stays
 * outside a composable lambda (composable calls aren't allowed inside `map { }`).
 */
@Composable
private fun phoneStateLabels(): Map<PhoneState, String> = mapOf(
    PhoneState.NORMAL to PhoneState.NORMAL.displayLabel(),
    PhoneState.VIBRATE to PhoneState.VIBRATE.displayLabel(),
    PhoneState.SILENT to PhoneState.SILENT.displayLabel(),
    PhoneState.DO_NOT_DISTURB to PhoneState.DO_NOT_DISTURB.displayLabel(),
    PhoneState.AIRPLANE_MODE to PhoneState.AIRPLANE_MODE.displayLabel()
)

/**
 * Suspend resolver for non-composable contexts (e.g. building a snackbar message
 * inside a coroutine). Uses the suspend `getString` from Compose Resources.
 */
suspend fun UiText.resolveString(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> {
        val resolvedArgs = ArrayList<Any>(args.size)
        for (arg in args) {
            resolvedArgs.add(if (arg is PhoneState) getString(arg.labelRes()) else arg)
        }
        getString(resource, *resolvedArgs.toTypedArray())
    }
}
