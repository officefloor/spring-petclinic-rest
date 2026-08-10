package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Rolls a date forward onto a business day: weekends (Saturday and Sunday) and listed public
 * holidays are skipped, rolling forward one day at a time until a non-holiday weekday is reached,
 * while a plain weekday is left unchanged. Used to keep the owner registration date (whether
 * supplied on the request or defaulted to the server date) on a business day.
 */
public final class BusinessDay {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /** Returns {@code date} unchanged when it is a non-holiday weekday, otherwise rolls forward to
     *  the next business day, skipping Saturdays, Sundays and listed public holidays. */
    public static LocalDate adjust(LocalDate date) {
        while (isWeekend(date) || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
