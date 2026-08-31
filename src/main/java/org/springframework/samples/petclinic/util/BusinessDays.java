package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day helper: a weekend or public-holiday date rolls forward to the next
 * non-holiday business day; ordinary weekdays are returned unchanged.
 */
public final class BusinessDays {

    /** Fixed public holidays the roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
        LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
        LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
        LocalDate.parse("2026-12-28"));

    private BusinessDays() {
    }

    /** Roll weekends and public holidays forward to the next business day. */
    public static LocalDate rollForward(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !HOLIDAYS.contains(date);
    }
}
