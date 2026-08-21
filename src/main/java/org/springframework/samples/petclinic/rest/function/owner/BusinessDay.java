package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day rules for an owner's registration date. The effective registration date — whether
 * supplied in the request or defaulted to the server date — must fall on a business day: a Saturday
 * or Sunday rolls forward to the following Monday. Everything derived from the registration date
 * (the membership number's year segment, the daily create-limit) uses this adjusted date.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /**
     * Rolls a weekend date forward to the next Monday; a weekday is returned unchanged.
     */
    static LocalDate adjust(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    /**
     * The effective, business-day-adjusted registration date: the supplied date when present,
     * otherwise the server's current date, rolled off any weekend to the next Monday.
     */
    static LocalDate effective(LocalDate supplied) {
        LocalDate base = (supplied != null) ? supplied : LocalDate.now();
        return adjust(base);
    }
}
