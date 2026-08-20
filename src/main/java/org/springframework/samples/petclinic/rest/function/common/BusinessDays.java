package org.springframework.samples.petclinic.rest.function.common;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day helpers shared across owner functions. A registration date must fall on a business
 * day: a Saturday or Sunday rolls forward to the next Monday, any weekday is kept unchanged.
 */
public final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * Rolls {@code date} forward to the next business day when it falls on a weekend, otherwise
     * returns it unchanged.
     */
    public static LocalDate rollToBusinessDay(LocalDate date) {
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
