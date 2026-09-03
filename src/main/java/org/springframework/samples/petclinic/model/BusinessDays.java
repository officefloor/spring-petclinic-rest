package org.springframework.samples.petclinic.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day helper: a registration date must fall on a business day, so a
 * Saturday or Sunday is rolled forward to the next Monday.
 */
public final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * Returns {@code date} unchanged when it is a weekday, or the following Monday
     * when it falls on a weekend.
     */
    public static LocalDate roll(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
