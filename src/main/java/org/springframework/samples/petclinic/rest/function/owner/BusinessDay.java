package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/** Rolls a weekend date forward to the following Monday; business days are returned unchanged. */
final class BusinessDay {

    private BusinessDay() {
    }

    static LocalDate nextBusinessDay(LocalDate date) {
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
