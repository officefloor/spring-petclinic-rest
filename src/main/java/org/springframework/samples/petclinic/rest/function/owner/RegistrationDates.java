package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * The effective registration date must fall on a business day: when it lands on a Saturday or
 * Sunday it rolls forward to the next Monday. Applied to the effective date whether it was
 * supplied in the request or defaulted to the server's current date, so every value derived from
 * the registration date (membership number year, daily create-limit) uses the adjusted date.
 */
final class RegistrationDates {

    private RegistrationDates() {
    }

    /** Roll a weekend date forward to the next Monday; a weekday is returned unchanged. */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
