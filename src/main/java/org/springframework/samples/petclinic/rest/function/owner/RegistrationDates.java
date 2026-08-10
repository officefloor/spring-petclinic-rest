package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * The effective registration date must fall on a business day: when it lands on a Saturday or
 * Sunday, or on a listed public holiday, it rolls forward to the next non-holiday business day.
 * Applied to the effective date whether it was supplied in the request or defaulted to the server's
 * current date, so every value derived from the registration date (membership number year, daily
 * create-limit) uses the adjusted date.
 */
final class RegistrationDates {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private RegistrationDates() {
    }

    /**
     * Roll a weekend or public-holiday date forward to the next non-holiday business day; a
     * business day that is not a holiday is returned unchanged.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
