package com.michaelmccormick.core.ui.extensions

import android.content.Context
import android.os.Build
import java.util.Locale

fun Context.getLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.resources.configuration.locales.get(0)
    } else {
        this.resources.configuration.locale
    }
}
