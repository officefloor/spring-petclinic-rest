package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day arithmetic for owner registration. A registration date must fall on a
 * business day: when it lands on a Saturday or Sunday it rolls forward to the next
 * Monday.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /**
     * Rolls {@code date} forward to the next business day: a Saturday or Sunday moves
     * to the following Monday; a weekday is returned unchanged.
     */
    static LocalDate roll(LocalDate date) {
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
