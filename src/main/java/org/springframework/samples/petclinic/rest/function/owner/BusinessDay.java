package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a date forward onto a business day: a Saturday or Sunday becomes the following Monday,
 * any weekday is unchanged. Used for the effective registration date and everything derived from it.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    public static LocalDate roll(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
