package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Rolls a registration date forward to a business day: a Saturday, Sunday or listed public
 * holiday is rolled forward to the next non-holiday weekday; an ordinary weekday is returned
 * unchanged. Used for the effective registration date (supplied on the request or defaulted to
 * the server date) so that the stored {@code registrationDate} — and everything derived from it
 * — always falls on a business day.
 */
public final class BusinessDay {

    /** Fixed public-holiday calendar. Dates falling on any of these roll forward. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDay() {
    }

    public static LocalDate roll(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
