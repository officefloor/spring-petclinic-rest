package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day rules for owner registration dates.
 *
 * <p>A registration date must fall on a business day. When the effective date (whether
 * supplied in the request or defaulted to the server date) lands on a Saturday, Sunday or
 * listed public holiday, it is rolled forward to the next non-holiday weekday.
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
     * Roll a date forward to a business day: a Saturday, Sunday or public holiday is rolled
     * forward day by day until a non-holiday weekday is reached; any other day is returned
     * unchanged.
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
