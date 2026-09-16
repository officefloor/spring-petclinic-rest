package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Registration dates must fall on a business day. A date landing on a Saturday or Sunday
 * rolls forward to the following Monday; a weekday is returned unchanged.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    /** The given date if it is a weekday, otherwise the next Monday. */
    static LocalDate rollForward(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
