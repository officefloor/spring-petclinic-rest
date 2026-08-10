package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day helper for owner registration. The effective registration date must fall on a
 * business day: a Saturday or Sunday rolls forward to the following Monday.
 */
public final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * Rolls {@code date} forward to the next business day when it falls on a weekend, leaving a
     * weekday unchanged. A Saturday rolls to the following Monday, a Sunday to the next day.
     */
    public static LocalDate rollForward(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
