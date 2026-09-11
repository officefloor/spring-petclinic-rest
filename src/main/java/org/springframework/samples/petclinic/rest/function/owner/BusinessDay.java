package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a registration date forward onto a business day. A Saturday or Sunday is
 * rolled forward to the next Monday; a weekday is returned unchanged. Shared by
 * {@link BuildOwner} (which stores the adjusted date, so every value derived from it
 * — such as the membership number's year segment — uses the business day) and
 * {@link CheckOwnerDailyLimit} (which counts owners per adjusted business day).
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /** Returns {@code date} if it is a weekday, else the next Monday. */
    static LocalDate adjust(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
