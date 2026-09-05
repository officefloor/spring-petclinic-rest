package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day helper for owner registration. The effective registration date must fall on a
 * business day: when it lands on a Saturday or Sunday it is rolled forward to the next Monday.
 * Applies to the effective registration date whether supplied in the request or defaulted to the
 * server date, and every value derived from that date (membership-number year, daily create-limit)
 * must use the adjusted date.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /** Roll a weekend date forward to the next Monday; a business day is returned unchanged. */
    static LocalDate adjust(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
