package com.rafaelaguerra.synctask.presentation.common

import org.jetbrains.compose.resources.StringResource

/**
 * UI-facing text that can be resolved in the presentation layer.
 * Keeps resource lookups out of ViewModels while preserving i18n.
 */
sealed class UiText {
    data class Dynamic(val value: String) : UiText()

    data class Resource(
        val resource: StringResource,
        val args: List<Any> = emptyList()
    ) : UiText() {
        constructor(resource: StringResource, vararg args: Any) : this(resource, args.toList())
    }
}
