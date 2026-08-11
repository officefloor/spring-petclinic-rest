package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Resolves the effective registration date for a new owner. The date is the one supplied in the
 * request when present, otherwise the server's current date; either way it must fall on a business
 * day, so a Saturday, Sunday or listed public holiday is rolled forward to the next non-holiday
 * business day.
 *
 * <p>Shared by {@link ApplyRegistrationDate} (which stores it on the owner and thereby feeds every
 * value derived from it, such as the membership number's year segment) and
 * {@link RequireDailyCapacity} (which counts owners per adjusted business day).
 */
final class RegistrationDate {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private RegistrationDate() {
    }

    /**
     * The effective registration date: {@code supplied} when non-null, otherwise today, rolled
     * forward to the next business day.
     */
    static LocalDate effective(LocalDate supplied) {
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }

    /**
     * Roll a weekend or public-holiday date forward to the next non-holiday business day; a plain
     * weekday is returned unchanged.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
