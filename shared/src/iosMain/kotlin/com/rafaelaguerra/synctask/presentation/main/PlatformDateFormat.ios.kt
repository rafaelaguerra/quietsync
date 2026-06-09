package com.rafaelaguerra.synctask.presentation.main

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSDateFormatterStyle
import platform.Foundation.dateWithTimeIntervalSince1970

private fun format(
    epochMillis: Long,
    dateStyle: NSDateFormatterStyle,
    timeStyle: NSDateFormatterStyle
): String {
    val formatter = NSDateFormatter()
    formatter.dateStyle = dateStyle
    formatter.timeStyle = timeStyle
    val date = NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0)
    return formatter.stringFromDate(date)
}

actual fun formatShortDate(epochMillis: Long): String =
    format(epochMillis, NSDateFormatterShortStyle, NSDateFormatterNoStyle)

actual fun formatMediumDate(epochMillis: Long): String =
    format(epochMillis, NSDateFormatterMediumStyle, NSDateFormatterNoStyle)

actual fun formatShortTime(epochMillis: Long): String =
    format(epochMillis, NSDateFormatterNoStyle, NSDateFormatterShortStyle)

actual fun formatShortDateTime(epochMillis: Long): String =
    format(epochMillis, NSDateFormatterShortStyle, NSDateFormatterShortStyle)
