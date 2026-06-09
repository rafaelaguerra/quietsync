package com.rafaelaguerra.synctask.presentation.main

import java.text.DateFormat
import java.util.Date

actual fun formatShortDate(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.SHORT).format(Date(epochMillis))

actual fun formatMediumDate(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))

actual fun formatShortTime(epochMillis: Long): String =
    DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(epochMillis))

actual fun formatShortDateTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(epochMillis))
