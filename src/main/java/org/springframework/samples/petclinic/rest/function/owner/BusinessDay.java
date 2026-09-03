package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Rolls a date forward to a business day: weekends and listed public holidays advance to
 * the next non-holiday weekday; any other weekday is returned unchanged.
 */
final class BusinessDay {

    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDay() {
    }

    static LocalDate rollForward(LocalDate date) {
        while (isNonBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isNonBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || HOLIDAYS.contains(date);
    }
}
