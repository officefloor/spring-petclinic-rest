package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * The single definition of how an owner registration date is moved onto a business day.
 * Shared by {@link ResolveOwnerRegistrationDate} (which stamps the new owner) and, through the
 * resolved date it publishes, by {@link RejectDailyOwnerLimit}, so a date and the day it is
 * counted against are rolled identically.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    /** Rolls a date that falls on a Saturday or Sunday forward to the next Monday; a date that
     * already falls on a weekday is returned unchanged. */
    static LocalDate rollForward(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
