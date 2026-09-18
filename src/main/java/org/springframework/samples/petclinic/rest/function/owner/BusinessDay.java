package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * The single definition of a "business day" for owner registration: a weekday (Monday–Friday)
 * that is not a listed public holiday. A date that falls on a weekend or a public holiday is
 * rolled forward to the next such business day; a plain weekday is returned unchanged. Used so
 * every effective registration date — whether supplied in the request or defaulted to the server
 * date — lands on a business day, and so the daily create-limit buckets owners by that same
 * adjusted day.
 */
final class BusinessDay {

    /** Fixed public-holiday calendar; a date landing on one of these rolls forward. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDay() {
    }

    /** {@code date} rolled forward to the next business day when it falls on a weekend or public holiday, else unchanged. */
    static LocalDate adjust(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
                && !PUBLIC_HOLIDAYS.contains(date);
    }
}
