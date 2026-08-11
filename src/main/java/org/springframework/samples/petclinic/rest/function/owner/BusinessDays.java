package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day helper for owner registration. The effective registration date must fall on a
 * business day: a Saturday, Sunday or listed public holiday rolls forward to the next
 * non-holiday weekday.
 */
public final class BusinessDays {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDays() {
    }

    /**
     * Rolls {@code date} forward to the next business day when it falls on a weekend or a listed
     * public holiday, leaving an ordinary weekday unchanged. A Saturday rolls to the following
     * Monday, a Sunday to the next day, and a holiday to the next non-holiday business day.
     */
    public static LocalDate rollForward(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
