package com.michaelmccormick.core.factories

import java.util.Calendar
import javax.inject.Inject

class CalendarFactory @Inject internal constructor() {
    fun now(): Calendar = Calendar.getInstance()
}
