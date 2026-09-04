package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Rolls a date forward onto a business day: a Saturday or Sunday becomes the
 * following Monday, while a weekday is returned unchanged.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    public static LocalDate onOrAfter(LocalDate date) {
        int day = date.getDayOfWeek().getValue(); // Mon=1 .. Sun=7
        return day > 5 ? date.plusDays(8 - day) : date;
    }
}
