package com.michaelmccormick.core.factories

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import java.util.Calendar
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class CalendarFactoryTest {
    private lateinit var calendarFactory: CalendarFactory

    @BeforeAll
    fun beforeAll() {
        mockkStatic(Calendar::class)
    }

    @BeforeEach
    fun before() {
        calendarFactory = CalendarFactory()
    }

    @Test
    fun `Should return current calender instance`() {
        // Given
        val mockCalendar: Calendar = mockk()
        every { Calendar.getInstance() } returns mockCalendar

        // When
        val calendar = calendarFactory.now()

        // Then
        assertEquals(mockCalendar, calendar)
    }
}
