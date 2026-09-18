package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day helpers for the owner registration date.
 */
public final class BusinessDay {

    /**
     * Fixed public holidays that are not treated as business days.
     */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDay() {
    }

    /**
     * Rolls a date forward to the next business day: weekends (Saturday and Sunday) and listed
     * public holidays are skipped, so a date that lands on either advances to the following
     * non-weekend, non-holiday day, while a date already on a business day is returned unchanged.
     */
    public static LocalDate rollForward(LocalDate date) {
        LocalDate adjusted = date;
        while (!isBusinessDay(adjusted)) {
            adjusted = adjusted.plusDays(1);
        }
        return adjusted;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return false;
        }
        return !PUBLIC_HOLIDAYS.contains(date);
    }
}
