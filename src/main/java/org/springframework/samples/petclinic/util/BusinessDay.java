package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a date forward onto a business day: a Saturday or Sunday becomes the next Monday, while a
 * weekday is left unchanged. Used to keep the owner registration date (whether supplied on the
 * request or defaulted to the server date) on a business day.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    /** Returns {@code date} unchanged when it is a weekday, or the following Monday when it falls on
     *  a Saturday or Sunday. */
    public static LocalDate adjust(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
