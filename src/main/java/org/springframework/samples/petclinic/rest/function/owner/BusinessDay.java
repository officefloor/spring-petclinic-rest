package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a registration date forward onto a business day: a Saturday or Sunday becomes the next
 * Monday, any weekday is left unchanged. Applied to the effective registration date (supplied or
 * defaulted) so every value derived from it — the membership number's year, the daily create-limit
 * bucket — sees the same adjusted day.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    public static LocalDate roll(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }
}
