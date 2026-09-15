package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day calendar rules for owner registration. A registration date must fall on a
 * business day; a Saturday or Sunday rolls forward to the following Monday. Used when
 * resolving the effective registration date ({@link ResolveRegistrationDate}) so that the
 * stored date, everything derived from it (e.g. the membership number's year segment) and
 * the per-day create limit all agree on the adjusted business day.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    /** The given date, or the next Monday when it falls on a Saturday or Sunday. */
    static LocalDate rollForward(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
