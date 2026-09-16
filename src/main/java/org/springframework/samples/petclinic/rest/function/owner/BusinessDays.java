package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day calendar rules for owner registration. A registration date must fall on a
 * business day; a Saturday, Sunday or listed public holiday rolls forward to the next
 * non-holiday weekday. Used when resolving the effective registration date
 * ({@link ResolveRegistrationDate}) so that the stored date, everything derived from it
 * (e.g. the membership number's year segment) and the per-day create limit all agree on
 * the adjusted business day.
 */
final class BusinessDays {

    /** Fixed public holidays that a registration date rolls past. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDays() {
    }

    /** The given date, or the next business day when it falls on a weekend or holiday. */
    static LocalDate rollForward(LocalDate date) {
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
