package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * The single definition of how an owner registration date is moved onto a business day.
 * Shared by {@link ResolveOwnerRegistrationDate} (which stamps the new owner) and, through the
 * resolved date it publishes, by {@link RejectDailyOwnerLimit}, so a date and the day it is
 * counted against are rolled identically.
 */
final class BusinessDays {

    /** Fixed public holidays that are not business days, rolled over like weekends. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private BusinessDays() {
    }

    /** Rolls a date that falls on a weekend or a listed public holiday forward to the next
     * business day; a date that is already a non-holiday weekday is returned unchanged. */
    static LocalDate rollForward(LocalDate date) {
        while (isWeekend(date) || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
