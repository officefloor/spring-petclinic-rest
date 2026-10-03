package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day helper for owner registration. A registration date must fall on a
 * business day: a Saturday or Sunday rolls forward to the following Monday. Single
 * source of truth for the effective registration date, so every caller (the stored
 * value and anything derived from it, such as the daily create-limit) agrees.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * The effective registration date: {@code supplied} when present, otherwise the
     * server's current date, rolled forward over a weekend to the following Monday.
     */
    static LocalDate effectiveRegistrationDate(LocalDate supplied) {
        return rollToBusinessDay(supplied != null ? supplied : LocalDate.now());
    }

    /** Rolls {@code date} forward to the next business day: a weekend becomes Monday. */
    private static LocalDate rollToBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
