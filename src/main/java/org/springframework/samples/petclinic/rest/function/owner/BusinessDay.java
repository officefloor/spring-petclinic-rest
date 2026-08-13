package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day arithmetic for owner registration. A registration date must fall on a
 * business day: when it lands on a Saturday, Sunday or listed public holiday it rolls
 * forward to the next non-holiday business day.
 */
final class BusinessDay {

    /** Fixed public holidays that the registration date rolls past. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"),
            LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /**
     * Rolls {@code date} forward to the next business day: a Saturday, Sunday or public
     * holiday moves to the following non-holiday weekday; a plain weekday is returned
     * unchanged.
     */
    static LocalDate roll(LocalDate date) {
        while (isNonBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isNonBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || HOLIDAYS.contains(date);
    }
}
