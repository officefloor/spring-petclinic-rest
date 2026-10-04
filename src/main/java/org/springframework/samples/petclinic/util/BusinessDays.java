package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Rolls a calendar date forward onto a business day.
 *
 * <p>A business day is any day that is not a Saturday, Sunday or listed public holiday. When the
 * supplied date already falls on a business day it is returned unchanged; otherwise it is rolled
 * forward one day at a time until it lands on a business day. The operation is idempotent: feeding
 * it an already-adjusted value returns that same value.
 */
public final class BusinessDays {

    /** Fixed public holidays that are skipped by the business-day roll. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"),
        LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"),
        LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    private BusinessDays() {
    }

    /**
     * Return {@code date} when it falls on a business day, otherwise roll it forward to the next
     * day that is neither a weekend nor a listed public holiday. Returns {@code null} when the
     * input is {@code null}.
     */
    public static LocalDate rollToBusinessDay(LocalDate date) {
        if (date == null) {
            return null;
        }
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !HOLIDAYS.contains(date);
    }

    /**
     * Resolve the effective registration date: the supplied date, or the server's current date
     * when {@code null}, rolled forward onto a business day. This is the single definition of the
     * "default-to-now, then roll" policy shared by the create controller (which needs the value
     * eagerly) and the {@code @PrePersist} backstop. Idempotent for an already-adjusted value.
     */
    public static LocalDate effectiveRegistrationDate(LocalDate supplied) {
        return rollToBusinessDay(supplied != null ? supplied : LocalDate.now());
    }
}
