package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day rules for owner registration dates.
 *
 * <p>A registration date must fall on a business day. When the effective date (whether
 * supplied in the request or defaulted to the server date) lands on a Saturday or Sunday,
 * it is rolled forward to the next Monday.
 */
public final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * Roll a date forward to a business day: a Saturday or Sunday becomes the next Monday;
     * any other day is returned unchanged.
     */
    public static LocalDate toBusinessDay(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
