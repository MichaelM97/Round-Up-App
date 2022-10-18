package com.michaelmccormick.core.factories

import java.util.UUID
import javax.inject.Inject

class UUIDFactory @Inject internal constructor() {
    fun random(): String = UUID.randomUUID().toString()
}
