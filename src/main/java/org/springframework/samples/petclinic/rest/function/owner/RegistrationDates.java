package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Shared rules for an owner's registration date. The <em>effective</em> registration date is the one
 * supplied in the request, or the server's current date when none was supplied; it must always fall on
 * a business day, so a Saturday or Sunday is rolled forward to the next Monday. Every value derived from
 * the registration date (the stored {@code registrationDate}, the membership number's year segment and
 * the per-day create-limit bucket) is computed from this adjusted date.
 */
final class RegistrationDates {

    private RegistrationDates() {
    }

    /**
     * The effective, business-day registration date: {@code supplied} when non-null, otherwise the
     * server's current date, rolled forward to the next Monday when it lands on a weekend.
     */
    static LocalDate effective(LocalDate supplied) {
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }

    /** Roll a weekend date forward to the next Monday; a weekday is returned unchanged. */
    static LocalDate toBusinessDay(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
