package org.springframework.samples.petclinic.rest.function.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Adjusts dates to business days. Registration dates must fall on a weekday that is not a public
 * holiday, so a date landing on a Saturday, Sunday or listed public holiday is rolled forward to the
 * next non-holiday weekday.
 */
public final class BusinessDay {

    /** Fixed public-holiday calendar; a date landing on one of these rolls forward. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"),
            LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /**
     * Rolls a weekend or public-holiday date forward to the next non-holiday weekday; a date that is
     * already a business day is returned unchanged.
     */
    public static LocalDate rollForward(LocalDate date) {
        LocalDate adjusted = date;
        while (isWeekend(adjusted) || PUBLIC_HOLIDAYS.contains(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
    }
}
