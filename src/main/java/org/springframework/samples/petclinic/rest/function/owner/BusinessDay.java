package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Set;

/** Business-day calendar: rolls a date forward over weekends and fixed public holidays. */
final class BusinessDay {

    /** Fixed public holidays that the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /** The given date, or the next day that is neither a weekend nor a listed holiday. */
    static LocalDate roll(LocalDate date) {
        while (date.getDayOfWeek().getValue() > 5 || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
