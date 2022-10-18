package com.michaelmccormick.core.factories

import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SimpleDateFormatFactoryTest {
    private lateinit var simpleDateFormatFactory: SimpleDateFormatFactory

    @BeforeEach
    fun before() {
        simpleDateFormatFactory = SimpleDateFormatFactory()
    }

    @Test
    fun `Should use correct format for iso8601 formatter`() {
        assertEquals(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            simpleDateFormatFactory.iso8601().toPattern(),
        )
    }

    @Test
    fun `Should use correct format for minIso8601 formatter`() {
        assertEquals(
            "yyyy-MM-dd'T'00:00:00.00'Z'",
            simpleDateFormatFactory.minIso8601().toPattern(),
        )
    }

    @Test
    fun `Should use correct format for maxIso8601 formatter`() {
        assertEquals(
            "yyyy-MM-dd'T'23:59:59.00'Z'",
            simpleDateFormatFactory.maxIso8601().toPattern(),
        )
    }
}
