package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Set;

/**
 * Rolls a date forward onto a business day: weekends and listed public holidays
 * roll forward to the next non-holiday weekday, while any other weekday is
 * returned unchanged.
 */
public final class BusinessDay {

    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    public static LocalDate onOrAfter(LocalDate date) {
        while (date.getDayOfWeek().getValue() > 5 || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
