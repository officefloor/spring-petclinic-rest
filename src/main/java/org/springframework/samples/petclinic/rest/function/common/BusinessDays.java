package org.springframework.samples.petclinic.rest.function.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day helpers shared across owner functions. A registration date must fall on a business
 * day: a Saturday, Sunday or listed public holiday rolls forward to the next non-holiday weekday,
 * any other weekday is kept unchanged.
 */
public final class BusinessDays {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDays() {
    }

    /**
     * Rolls {@code date} forward to the next business day when it falls on a weekend or a listed
     * public holiday, otherwise returns it unchanged. Consecutive weekend/holiday days are all
     * skipped.
     */
    public static LocalDate rollToBusinessDay(LocalDate date) {
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
