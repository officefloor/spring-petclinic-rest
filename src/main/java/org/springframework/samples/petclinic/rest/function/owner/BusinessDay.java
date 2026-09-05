package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a registration date onto a business day: a Saturday or Sunday moves forward to the
 * following Monday, any weekday is returned unchanged.
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
