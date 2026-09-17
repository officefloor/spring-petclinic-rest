package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessDaysTest {

    @Test
    void weekdayIsUnchanged() {
        LocalDate wednesday = LocalDate.of(2026, 9, 16);
        assertEquals(wednesday, BusinessDays.rollForward(wednesday));
    }

    @Test
    void weekendRollsToMonday() {
        assertEquals(LocalDate.of(2026, 9, 21), BusinessDays.rollForward(LocalDate.of(2026, 9, 19)));
        assertEquals(LocalDate.of(2026, 9, 21), BusinessDays.rollForward(LocalDate.of(2026, 9, 20)));
    }

    @Test
    void weekdayHolidayRollsToNextBusinessDay() {
        // 2026-04-25 is a Saturday holiday; 2026-01-26 is a Monday holiday.
        assertEquals(LocalDate.of(2026, 1, 27), BusinessDays.rollForward(LocalDate.of(2026, 1, 26)));
    }

    @Test
    void newYearsDayHolidayRollsForward() {
        // 2026-01-01 is a Thursday holiday; 2026-01-02 is a business day.
        assertEquals(LocalDate.of(2026, 1, 2), BusinessDays.rollForward(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void rollsPastConsecutiveWeekendAndHolidays() {
        // 2026-12-25 Fri holiday -> Sat -> Sun -> 2026-12-28 Mon holiday -> 2026-12-29 Tue.
        assertEquals(LocalDate.of(2026, 12, 29), BusinessDays.rollForward(LocalDate.of(2026, 12, 25)));
    }
}
