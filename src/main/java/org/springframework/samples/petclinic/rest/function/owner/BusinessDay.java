package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a date forward to a business day: a Saturday or Sunday advances to the next
 * Monday, any weekday is returned unchanged.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    static LocalDate rollForward(LocalDate date) {
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
