package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * The registration date must fall on a business day. A date landing on a Saturday or Sunday rolls
 * forward to the following Monday; a weekday is left unchanged. Applied to the effective registration
 * date — whether supplied in the request or defaulted to the server date — so every value derived
 * from the registration date (membership number year, per-day create limit) sees the adjusted date.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    /** Roll a weekend date forward to the next Monday; return a weekday unchanged. */
    static LocalDate rollForward(LocalDate date) {
        LocalDate rolled = date;
        while (rolled.getDayOfWeek() == DayOfWeek.SATURDAY
                || rolled.getDayOfWeek() == DayOfWeek.SUNDAY) {
            rolled = rolled.plusDays(1);
        }
        return rolled;
    }
}
