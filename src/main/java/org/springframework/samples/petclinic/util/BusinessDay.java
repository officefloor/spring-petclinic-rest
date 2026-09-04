package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a date forward onto a business day: a Saturday or Sunday advances to the
 * following Monday, while a weekday is returned unchanged.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    /** {@code date} unchanged when it is a weekday, otherwise the following Monday. */
    public static LocalDate rollForward(LocalDate date) {
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
