package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day helper. A registration date must fall on a business day, so a weekend day or a
 * listed {@link #PUBLIC_HOLIDAYS public holiday} rolls forward to the next business day.
 */
final class BusinessDays {

    /** Fixed public-holiday calendar; a date landing on one of these is not a business day. */
    static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDays() {
    }

    /**
     * Rolls {@code date} forward onto a business day: a weekend day or a public holiday moves to
     * the next non-weekend, non-holiday day; a business day is returned unchanged.
     */
    static LocalDate rollForward(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY
                && date.getDayOfWeek() != DayOfWeek.SUNDAY
                && !PUBLIC_HOLIDAYS.contains(date);
    }
}
