package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Resolves the effective registration date for a new owner. The date is the one supplied in the
 * request when present, otherwise the server's current date; either way it must fall on a business
 * day, so a Saturday or Sunday is rolled forward to the following Monday.
 *
 * <p>Shared by {@link ApplyRegistrationDate} (which stores it on the owner and thereby feeds every
 * value derived from it, such as the membership number's year segment) and
 * {@link RequireDailyCapacity} (which counts owners per adjusted business day).
 */
final class RegistrationDate {

    private RegistrationDate() {
    }

    /**
     * The effective registration date: {@code supplied} when non-null, otherwise today, rolled
     * forward to the next business day.
     */
    static LocalDate effective(LocalDate supplied) {
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }

    /** Roll a weekend date forward to the following Monday; a weekday is returned unchanged. */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
