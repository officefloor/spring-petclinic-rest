package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day helpers for the owner registration date.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    /**
     * Rolls a weekend date forward to the following Monday: a Saturday or Sunday becomes the
     * next Monday, while a weekday is returned unchanged.
     */
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
