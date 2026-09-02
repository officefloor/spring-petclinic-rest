package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/** Rolls a weekend or public-holiday date forward to the next business day; business days
 * are returned unchanged. */
final class BusinessDay {

    /** Fixed public holidays the roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    static LocalDate nextBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        while (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
            day = date.getDayOfWeek();
        }
        return date;
    }
}
