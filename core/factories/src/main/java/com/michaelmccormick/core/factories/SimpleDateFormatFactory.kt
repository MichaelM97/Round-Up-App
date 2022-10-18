package com.michaelmccormick.core.factories

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class SimpleDateFormatFactory @Inject internal constructor() {
    fun iso8601() = iso8601(timePattern = "HH:mm:ss.SSS")

    fun minIso8601() = iso8601(timePattern = "00:00:00.00")

    fun maxIso8601() = iso8601(timePattern = "23:59:59.00")

    private fun iso8601(timePattern: String): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'$timePattern'Z'", Locale.ROOT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
}
