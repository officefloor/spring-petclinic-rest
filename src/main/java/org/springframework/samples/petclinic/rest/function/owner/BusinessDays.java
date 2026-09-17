package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Registration dates must fall on a business day. A date landing on a Saturday, a Sunday or
 * a listed public holiday rolls forward to the next non-holiday weekday; a plain weekday is
 * returned unchanged.
 */
final class BusinessDays {

    /** Fixed public holidays on which registration dates may not fall. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDays() {
    }

    /** The given date if it is a business day, otherwise the next business day. */
    static LocalDate rollForward(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        return !isWeekend(date) && !PUBLIC_HOLIDAYS.contains(date);
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
