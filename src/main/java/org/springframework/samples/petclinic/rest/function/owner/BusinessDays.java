package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Shared business-day logic. A business day is a Monday–Friday that is not a public
 * holiday; a Saturday, Sunday or listed public holiday rolls forward to the next
 * business day. Every rule that must land on a business day (the registration date, and
 * therefore the daily limit and membership number derived from it) uses this single
 * definition.
 */
final class BusinessDays {

    /**
     * Fixed public-holiday calendar. Any date here is not a business day and is rolled
     * past, just like a weekend.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDays() {
    }

    /**
     * The given date if it is a business day, otherwise the next business day: weekends
     * and public holidays roll forward, one day at a time, until a non-holiday weekday
     * is reached.
     */
    static LocalDate rollForward(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY
                && !PUBLIC_HOLIDAYS.contains(date);
    }
}
