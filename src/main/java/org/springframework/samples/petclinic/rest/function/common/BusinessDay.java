package org.springframework.samples.petclinic.rest.function.common;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Adjusts dates to business days. Registration dates must fall on a weekday, so a date landing on a
 * Saturday or Sunday is rolled forward to the following Monday.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    /**
     * Rolls a weekend date forward to the next Monday; a weekday is returned unchanged.
     */
    public static LocalDate rollForward(LocalDate date) {
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
