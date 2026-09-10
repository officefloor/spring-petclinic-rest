package org.springframework.samples.petclinic.rest.function.common;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a date forward onto a business day. The registration date of an owner must
 * fall on a business day: a Saturday or Sunday is rolled forward to the following
 * Monday. Any value derived from the registration date (e.g. the membership number's
 * year segment) therefore uses the adjusted date.
 */
public final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * Adjust a date onto a business day: when it falls on a weekend, roll it forward to
     * the next Monday; otherwise return it unchanged.
     */
    public static LocalDate adjust(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
