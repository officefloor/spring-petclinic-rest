package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Unit tests for the shared business-day rule, including the public-holiday roll. */
class BusinessDaysTest {

    @Test
    void weekdayIsUnchanged() {
        LocalDate wednesday = LocalDate.of(2026, 2, 4);
        assertEquals(wednesday, BusinessDays.rollForward(wednesday));
    }

    @Test
    void saturdayRollsToMonday() {
        assertEquals(LocalDate.of(2026, 2, 9), BusinessDays.rollForward(LocalDate.of(2026, 2, 7)));
    }

    @Test
    void sundayRollsToMonday() {
        assertEquals(LocalDate.of(2026, 2, 9), BusinessDays.rollForward(LocalDate.of(2026, 2, 8)));
    }

    @Test
    void holidayOnWeekdayRollsToNextBusinessDay() {
        // 2026-01-26 (holiday) is a Monday; the following Tuesday is a business day.
        assertEquals(LocalDate.of(2026, 1, 27), BusinessDays.rollForward(LocalDate.of(2026, 1, 26)));
    }

    @Test
    void holidayFallingOnFridayRollsPastTheWeekend() {
        // 2026-04-25 (holiday) is a Saturday; roll through the weekend to Monday.
        assertEquals(LocalDate.of(2026, 4, 27), BusinessDays.rollForward(LocalDate.of(2026, 4, 25)));
    }

    @Test
    void consecutiveHolidaysAreAllRolledPast() {
        // 2026-12-25 (holiday, Friday) -> weekend -> 2026-12-28 (holiday, Monday) -> Tuesday.
        assertEquals(LocalDate.of(2026, 12, 29), BusinessDays.rollForward(LocalDate.of(2026, 12, 25)));
    }

    @Test
    void newYearsDayRollsForward() {
        // 2026-01-01 is a Thursday holiday; the next business day is Friday.
        assertEquals(LocalDate.of(2026, 1, 2), BusinessDays.rollForward(LocalDate.of(2026, 1, 1)));
    }
}
