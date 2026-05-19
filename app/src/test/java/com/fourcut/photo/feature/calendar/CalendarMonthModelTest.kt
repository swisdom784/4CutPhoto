package com.fourcut.photo.feature.calendar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalendarMonthModelTest {
    @Test
    fun monthModelUsesSundayBasedLeadingBlankCount() {
        val model = buildCalendarMonthUiModel(
            selectedDate = LocalDate.of(2026, 5, 19),
            sessionDates = emptyList()
        )

        assertEquals("2026년 5월", model.monthLabel)
        assertEquals(5, model.leadingBlankCount)
        assertEquals(31, model.days.size)
    }

    @Test
    fun monthModelOnlyMarksSessionsFromVisibleMonth() {
        val model = buildCalendarMonthUiModel(
            selectedDate = LocalDate.of(2026, 5, 19),
            sessionDates = listOf(
                LocalDate.of(2026, 5, 19),
                LocalDate.of(2026, 4, 19)
            )
        )

        assertEquals(true, model.days.first { it.dayOfMonth == 19 }.hasSessions)
        assertEquals(false, model.days.first { it.dayOfMonth == 18 }.hasSessions)
    }

    @Test
    fun monthModelCountsSessionsForEachDay() {
        val model = buildCalendarMonthUiModel(
            selectedDate = LocalDate.of(2026, 5, 19),
            sessionDates = listOf(
                LocalDate.of(2026, 5, 19),
                LocalDate.of(2026, 5, 19),
                LocalDate.of(2026, 5, 20)
            )
        )

        assertEquals(2, model.days.first { it.dayOfMonth == 19 }.sessionCount)
        assertEquals(1, model.days.first { it.dayOfMonth == 20 }.sessionCount)
        assertEquals(0, model.days.first { it.dayOfMonth == 21 }.sessionCount)
    }
}
