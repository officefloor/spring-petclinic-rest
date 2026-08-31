package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day helper: a weekend date rolls forward to the following Monday;
 * weekday dates are returned unchanged.
 */
public final class BusinessDays {

    private BusinessDays() {
    }

    /** Roll a Saturday/Sunday forward to the next Monday; a weekday is returned unchanged. */
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
