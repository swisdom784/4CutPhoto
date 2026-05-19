package com.fourcut.photo.feature.calendar

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CalendarMonthUiModel(
    val monthLabel: String,
    val leadingBlankCount: Int,
    val days: List<CalendarDayUiModel>
)

fun buildCalendarMonthUiModel(
    selectedDate: LocalDate,
    sessionDates: List<LocalDate>
): CalendarMonthUiModel {
    val firstDayOfMonth = selectedDate.withDayOfMonth(1)
    val visibleYearMonth = selectedDate.year to selectedDate.month
    val days = (1..selectedDate.lengthOfMonth()).map { day ->
        val date = selectedDate.withDayOfMonth(day)
        val sessionCount = sessionDates.count { sessionDate ->
            sessionDate == date && (sessionDate.year to sessionDate.month) == visibleYearMonth
        }
        CalendarDayUiModel(
            dayOfMonth = day,
            hasSessions = sessionCount > 0,
            isSelected = day == selectedDate.dayOfMonth,
            sessionCount = sessionCount
        )
    }

    return CalendarMonthUiModel(
        monthLabel = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)),
        leadingBlankCount = firstDayOfMonth.dayOfWeek.value % 7,
        days = days
    )
}
