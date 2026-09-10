package org.springframework.samples.petclinic.rest.function.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Rolls a date forward onto a business day. The registration date of an owner must
 * fall on a business day: a Saturday, Sunday or listed public holiday is rolled forward
 * to the next non-holiday business day. Any value derived from the registration date
 * (e.g. the membership number's year segment) therefore uses the adjusted date.
 */
public final class BusinessDays {

    /**
     * Fixed list of public holidays that are not business days.
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
     * Adjust a date onto a business day: when it falls on a weekend or a listed public
     * holiday, roll it forward to the next non-holiday business day; otherwise return it
     * unchanged.
     */
    public static LocalDate adjust(LocalDate date) {
        while (isWeekend(date) || PUBLIC_HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
