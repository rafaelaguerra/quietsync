package com.rafaelaguerra.synctask.platform

import platform.Foundation.NSUserDefaults

object IosAppPreferences {
    private val defaults = NSUserDefaults.standardUserDefaults

    fun hasSeenOnboarding(): Boolean =
        defaults.boolForKey(KEY_HAS_SEEN_ONBOARDING)

    fun setOnboardingSeen() {
        defaults.setBool(true, KEY_HAS_SEEN_ONBOARDING)
    }

    fun swipeHintShownCount(): Int =
        defaults.integerForKey(KEY_SWIPE_HINT_SHOWN_COUNT).toInt()

    fun incrementSwipeHintShownCount(): Int {
        val next = swipeHintShownCount() + 1
        defaults.setInteger(next.toLong(), KEY_SWIPE_HINT_SHOWN_COUNT)
        return next
    }

    private const val KEY_HAS_SEEN_ONBOARDING = "key_has_seen_onboarding"
    private const val KEY_SWIPE_HINT_SHOWN_COUNT = "key_swipe_hint_shown_count"
}
