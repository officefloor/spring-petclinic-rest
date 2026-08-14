package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Shared business-day adjustment for the create-owner pipeline. The effective registration date
 * must fall on a business day: when it lands on a Saturday, Sunday or a listed public holiday it
 * rolls forward to the next non-holiday weekday. Ordinary weekdays are kept unchanged and the
 * transform is idempotent.
 *
 * <p>Used for the single effective registration date so every value derived from it — what is
 * stored and returned, the membership number's year segment and the daily create-limit's per-day
 * bucket — agrees on the same adjusted date.
 */
public final class BusinessDays {

    /** Fixed public-holiday calendar; a date landing on one of these rolls forward. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDays() {
    }

    /**
     * Roll {@code date} forward to the next business day — a weekday that is not a listed public
     * holiday — when it falls on a weekend or holiday; otherwise keep it.
     */
    public static LocalDate adjust(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return false;
        }
        return !PUBLIC_HOLIDAYS.contains(date);
    }
}
