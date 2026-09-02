package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day calendar helper. A registration date must fall on a business day, so a
 * Saturday or Sunday rolls forward to the following Monday.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    /** The given date, or the next Monday when it falls on a weekend. */
    public static LocalDate roll(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
