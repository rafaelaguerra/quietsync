package com.rafaelaguerra.synctask.domain.model

enum class Weekday(val rruleCode: String) {
    MONDAY("MO"),
    TUESDAY("TU"),
    WEDNESDAY("WE"),
    THURSDAY("TH"),
    FRIDAY("FR"),
    SATURDAY("SA"),
    SUNDAY("SU");

    companion object {
        fun fromRruleCode(code: String): Weekday? =
            entries.find { it.rruleCode == code }
    }
}
