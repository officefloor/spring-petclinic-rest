package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a registration date forward onto a business day: a Saturday or Sunday moves forward to the
 * next Monday; a weekday is returned unchanged. Applied to the EFFECTIVE registration date (whether
 * supplied in the request or defaulted to the server date) so that every stored {@code
 * registrationDate}, and every value derived from it, falls on a business day.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    public static LocalDate adjust(LocalDate date) {
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
