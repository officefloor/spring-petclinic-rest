package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a registration date forward to a business day: a Saturday or Sunday is rolled
 * forward to the following Monday; a weekday is returned unchanged. Used for the effective
 * registration date (supplied on the request or defaulted to the server date) so that the
 * stored {@code registrationDate} — and everything derived from it — always falls on a
 * business day.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    public static LocalDate roll(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
