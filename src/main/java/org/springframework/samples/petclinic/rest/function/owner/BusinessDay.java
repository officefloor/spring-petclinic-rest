package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day adjustment for the owner registration date. A registration date must fall on a
 * business day: when it lands on a Saturday or Sunday it rolls forward to the next Monday. Applied
 * to the effective registration date (supplied or defaulted to the server date) so every value
 * derived from it — the membership number's year segment, the daily create-limit count — uses the
 * adjusted date.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /** Roll a weekend date forward to the next Monday; any weekday is returned unchanged. */
    static LocalDate roll(LocalDate date) {
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
