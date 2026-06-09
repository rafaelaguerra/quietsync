package com.rafaelaguerra.synctask.presentation.main

/** Locale-aware date/time formatting, backed by the platform formatter on each target. */
expect fun formatShortDate(epochMillis: Long): String
expect fun formatMediumDate(epochMillis: Long): String
expect fun formatShortTime(epochMillis: Long): String
expect fun formatShortDateTime(epochMillis: Long): String
