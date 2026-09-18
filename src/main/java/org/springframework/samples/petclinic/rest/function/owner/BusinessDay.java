package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day rules for registration dates. A registration date must fall on a business
 * day: not a Saturday or Sunday, and not one of the fixed public holidays. A date landing on
 * a weekend or a public holiday rolls forward to the next non-holiday business day.
 */
public final class BusinessDay {

    /** Fixed public holidays that the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /**
     * Rolls {@code date} forward to the next business day when it falls on a weekend or a
     * public holiday; a date that already lands on a business day is returned unchanged.
     */
    public static LocalDate rollForward(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !HOLIDAYS.contains(date);
    }
}
