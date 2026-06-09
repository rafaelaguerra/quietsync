@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.rafaelaguerra.synctask.platform

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

object IosUrlOpener {
    fun open(urlString: String): Boolean {
        val url = NSURL.URLWithString(urlString) ?: return false
        return UIApplication.sharedApplication.openURL(url)
    }

    fun buildMailtoUrl(email: String, subject: String, body: String): String {
        return buildString {
            append("mailto:")
            append(email)
            append("?subject=")
            append(percentEncode(subject))
            append("&body=")
            append(percentEncode(body))
        }
    }

    private fun percentEncode(value: String): String = buildString {
        value.forEach { char ->
            when {
                char.isLetterOrDigit() || char in "-_.~" -> append(char)
                char == ' ' -> append("%20")
                char == '\n' -> append("%0A")
                else -> char.code.toString(16).uppercase().let { hex ->
                    append('%')
                    if (hex.length == 1) append('0')
                    append(hex)
                }
            }
        }
    }
}
