package org.springframework.samples.petclinic.util;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessDayTest {

    @Test
    void weekdayIsUnchanged() {
        LocalDate wednesday = LocalDate.of(2026, 9, 16);
        assertEquals(wednesday, BusinessDay.rollForward(wednesday));
    }

    @Test
    void saturdayRollsToMonday() {
        assertEquals(LocalDate.of(2026, 9, 21), BusinessDay.rollForward(LocalDate.of(2026, 9, 19)));
    }

    @Test
    void sundayRollsToMonday() {
        assertEquals(LocalDate.of(2026, 9, 21), BusinessDay.rollForward(LocalDate.of(2026, 9, 20)));
    }

    @Test
    void holidayOnWeekdayRollsToNextBusinessDay() {
        // 2026-04-25 (public holiday) falls on a Saturday, 26th Sunday, so roll to Monday 27th.
        assertEquals(LocalDate.of(2026, 4, 27), BusinessDay.rollForward(LocalDate.of(2026, 4, 25)));
    }

    @Test
    void holidayFallingOnWeekdayRollsForward() {
        // 2026-01-01 is a Thursday public holiday, so roll to Friday 2nd.
        assertEquals(LocalDate.of(2026, 1, 2), BusinessDay.rollForward(LocalDate.of(2026, 1, 1)));
    }

    @Test
    void consecutiveWeekendAndHolidaysAreAllSkipped() {
        // 2026-12-25 (Fri holiday), 26th Sat, 27th Sun, 28th Mon holiday -> roll to Tuesday 29th.
        assertEquals(LocalDate.of(2026, 12, 29), BusinessDay.rollForward(LocalDate.of(2026, 12, 25)));
    }
}
