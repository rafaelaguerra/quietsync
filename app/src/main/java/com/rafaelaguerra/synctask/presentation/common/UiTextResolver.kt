package com.rafaelaguerra.synctask.presentation.common

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.rafaelaguerra.synctask.domain.model.PhoneState
import com.rafaelaguerra.synctask.presentation.localization.displayLabel

fun UiText.resolve(context: Context): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> {
        val resolvedArgs = args.map { resolveFormatArg(context, it) }.toTypedArray()
        if (resolvedArgs.isEmpty()) {
            context.getString(resId)
        } else {
            context.getString(resId, *resolvedArgs)
        }
    }
}

@Composable
fun UiText.asString(): String = resolve(LocalContext.current)

private fun resolveFormatArg(context: Context, arg: Any): Any = when (arg) {
    is PhoneState -> arg.displayLabel(context)
    else -> arg
}
