package com.rafaelaguerra.synctask.presentation.onboarding

/** Which permission explanations to show on the onboarding permissions page. */
enum class OnboardingPermissionMode {
    /** Calendar + phone mode permissions (Android). */
    FULL,

    /** Calendar only (iOS — phone modes are not automated on this platform). */
    CALENDAR_ONLY
}
