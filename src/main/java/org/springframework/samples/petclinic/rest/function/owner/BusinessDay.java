package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day adjustment for the owner registration date. A registration date must fall on a
 * business day: when it lands on a Saturday, Sunday or a listed public holiday it rolls forward to
 * the next non-holiday weekday. Applied to the effective registration date (supplied or defaulted to
 * the server date) so every value derived from it — the membership number's year segment, the daily
 * create-limit count — uses the adjusted date.
 */
final class BusinessDay {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /**
     * Roll a date forward over weekends and public holidays to the next non-holiday weekday; a date
     * already on a business day is returned unchanged.
     */
    static LocalDate roll(LocalDate date) {
        while (isWeekend(date) || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}
