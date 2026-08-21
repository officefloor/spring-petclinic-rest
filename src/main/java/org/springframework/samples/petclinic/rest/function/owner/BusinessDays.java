package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Registration dates must fall on a business day. A date landing on a Saturday or Sunday rolls
 * forward to the following Monday; a weekday is returned unchanged.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    static LocalDate adjust(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dow == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
