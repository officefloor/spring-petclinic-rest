package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a date forward onto a business day. A Saturday or Sunday is rolled forward to the
 * following Monday; a weekday is returned unchanged. Used for the effective registration date
 * (supplied or defaulted) and everything derived from it.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    static LocalDate rollForward(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
