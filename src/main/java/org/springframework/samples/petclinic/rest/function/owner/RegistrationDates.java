package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Registration-date business rule: the effective registration date must fall on a business day.
 * A Saturday or Sunday rolls forward to the next Monday. Shared by {@link BuildOwner} (which
 * stamps the stored date) and {@link CheckOwnerDailyLimit} (which counts per business day) so
 * both agree on the adjusted date.
 */
final class RegistrationDates {

    private RegistrationDates() {
    }

    /**
     * The effective registration date: the supplied date, or the server's current date when none
     * was supplied, rolled forward off a weekend to the next business day.
     */
    static LocalDate effective(LocalDate supplied) {
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }

    /** Roll a Saturday or Sunday forward to the next Monday; any other day is returned unchanged. */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
