package org.springframework.samples.petclinic.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day helper: a registration date must fall on a business day, so a
 * Saturday, Sunday, or public holiday is rolled forward to the next business day.
 */
public final class BusinessDays {

    private static final Set<LocalDate> HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    private BusinessDays() {
    }

    private static boolean isBusinessDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY
            && !HOLIDAYS.contains(date);
    }

    /**
     * Returns {@code date} unchanged when it is a business day, or the next business
     * day when it falls on a weekend or a public holiday.
     */
    public static LocalDate roll(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
