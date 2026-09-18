package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Business-day rules for registration dates. A registration date must fall on a business
 * day (Monday–Friday); a Saturday or Sunday rolls forward to the following Monday.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    /**
     * Rolls {@code date} forward to the next Monday when it falls on a weekend; a date that
     * already lands on a business day is returned unchanged.
     */
    public static LocalDate rollForward(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return date.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        return date;
    }
}
