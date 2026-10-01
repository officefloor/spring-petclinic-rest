package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day rules for owner registration on {@code POST /api/owners}. The effective registration
 * date must fall on a business day: when it is a Saturday or Sunday it rolls forward to the following
 * Monday. The effective date is the one supplied in the request, or the server's current date when the
 * request omits it; either way the same roll-forward applies, so every value derived from the
 * registration date (the membership number's year segment, the per-day create-limit bucket) is computed
 * from the adjusted date.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /** Rolls a weekend date forward to the following Monday; a weekday is returned unchanged. */
    static LocalDate rollForward(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dow == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    /**
     * The effective, business-day registration date for a create: the supplied date when present,
     * otherwise the server's current date, rolled forward off any weekend.
     */
    static LocalDate effective(LocalDate supplied) {
        return rollForward(supplied != null ? supplied : LocalDate.now());
    }
}
