package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day calendar helper. A registration date must fall on a business day, so a
 * Saturday, Sunday or public holiday rolls forward to the next business day.
 */
public final class BusinessDay {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    private static boolean isBusinessDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY
                && !HOLIDAYS.contains(date);
    }

    /** The given date, or the next business day when it falls on a weekend or holiday. */
    public static LocalDate roll(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
