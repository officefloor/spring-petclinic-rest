package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * The registration date must fall on a business day. Computes the EFFECTIVE registration date for a
 * create: the date supplied on the request, or the server's current date when none was supplied,
 * rolled forward to the next Monday when it lands on a Saturday or Sunday. Every value derived from
 * the registration date (the membership number's year segment, the daily create-limit's day) uses
 * this adjusted date.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    /**
     * The effective registration date for a create: {@code supplied} when present, otherwise the
     * server's current date, rolled forward off a weekend to the next Monday.
     */
    public static LocalDate effectiveRegistrationDate(LocalDate supplied) {
        return rollForward(supplied != null ? supplied : LocalDate.now());
    }

    /** Rolls a Saturday or Sunday forward to the next Monday; a business day is returned unchanged. */
    public static LocalDate rollForward(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
