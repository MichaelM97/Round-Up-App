package com.michaelmccormick.core.factories

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UUIDFactoryTest {
    private lateinit var uuidFactory: UUIDFactory

    @BeforeAll
    fun beforeAll() {
        mockkStatic(UUID::class)
    }

    @BeforeEach
    fun before() {
        uuidFactory = UUIDFactory()
    }

    @Test
    fun `Should return random uuid string`() {
        // Given
        val uuid = "123-abc-xyz?1"
        every { UUID.randomUUID().toString() } returns uuid

        // When
        val result = uuidFactory.random()

        // Then
        assertEquals(uuid, result)
    }
}
