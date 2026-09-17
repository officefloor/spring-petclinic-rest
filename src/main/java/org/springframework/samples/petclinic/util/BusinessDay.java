package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day calendar rules. A business day is any weekday that is not a listed public holiday;
 * Saturdays, Sundays and public holidays are not business days. Pure date arithmetic with no
 * dependency on other state, so it is a shared helper rather than a step.
 */
public final class BusinessDay {

    /** Fixed public-holiday calendar. Dates that fall on one of these are not business days. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDay() {
    }

    /** Whether {@code date} is a public holiday. */
    public static boolean isPublicHoliday(LocalDate date) {
        return PUBLIC_HOLIDAYS.contains(date);
    }

    /** Whether {@code date} is a business day (a weekday that is not a public holiday). */
    public static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !isPublicHoliday(date);
    }

    /**
     * {@code date} itself when it already falls on a business day, otherwise the next business day:
     * a Saturday, Sunday or public holiday rolls forward to the following non-holiday weekday.
     */
    public static LocalDate onOrAfter(LocalDate date) {
        LocalDate result = date;
        while (!isBusinessDay(result)) {
            result = result.plusDays(1);
        }
        return result;
    }
}
