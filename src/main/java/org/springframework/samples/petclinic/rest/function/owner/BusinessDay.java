package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day rules for an owner's registration date. The effective registration date — whether
 * supplied in the request or defaulted to the server date — must fall on a business day: a Saturday,
 * a Sunday or a listed public holiday rolls forward to the next non-holiday weekday. Everything
 * derived from the registration date (the membership number's year segment, the daily create-limit)
 * uses this adjusted date.
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

    /**
     * Rolls a date forward to the next non-holiday business day. A weekday that is not a public
     * holiday is returned unchanged; a weekend or a listed public holiday advances one day at a time
     * until a business day is reached (so runs of adjacent holidays and weekends are all skipped).
     */
    static LocalDate adjust(LocalDate date) {
        LocalDate adjusted = date;
        while (isNonBusinessDay(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    private static boolean isNonBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
                || PUBLIC_HOLIDAYS.contains(date);
    }

    /**
     * The effective, business-day-adjusted registration date: the supplied date when present,
     * otherwise the server's current date, rolled off any weekend to the next Monday.
     */
    static LocalDate effective(LocalDate supplied) {
        LocalDate base = (supplied != null) ? supplied : LocalDate.now();
        return adjust(base);
    }
}
