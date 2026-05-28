package com.rafaelaguerra.synctask.presentation.common

import androidx.annotation.StringRes

/**
 * UI-facing text that can be resolved in the presentation layer.
 * Keeps Android resources out of ViewModels while preserving i18n.
 */
sealed class UiText {
    data class Dynamic(val value: String) : UiText()

    data class Resource(
        @StringRes val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText() {
        constructor(@StringRes resId: Int, vararg args: Any) : this(resId, args.toList())
    }
}
