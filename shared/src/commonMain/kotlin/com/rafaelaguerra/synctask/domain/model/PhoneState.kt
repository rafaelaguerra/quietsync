package com.rafaelaguerra.synctask.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PhoneState {
    NORMAL,
    VIBRATE,
    SILENT,
    DO_NOT_DISTURB,
    AIRPLANE_MODE
}
