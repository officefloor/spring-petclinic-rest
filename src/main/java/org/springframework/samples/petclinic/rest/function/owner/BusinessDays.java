package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Shared business-day logic. A business day is Monday–Friday; a Saturday or Sunday
 * rolls forward to the following Monday. Every rule that must land on a business day
 * (the registration date, and therefore the daily limit and membership number derived
 * from it) uses this single definition.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * The given date if it is a business day, otherwise the following Monday.
     */
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
