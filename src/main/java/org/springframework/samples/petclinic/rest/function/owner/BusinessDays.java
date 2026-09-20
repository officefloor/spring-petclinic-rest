package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day helper. A registration date must fall on a business day, so a Saturday or
 * Sunday rolls forward to the following Monday.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * Rolls {@code date} forward onto a business day: a weekend day moves to the following
     * Monday, any weekday is returned unchanged.
     */
    static LocalDate rollForward(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
