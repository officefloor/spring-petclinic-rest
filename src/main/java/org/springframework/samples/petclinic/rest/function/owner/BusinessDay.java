package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Rolls a registration date forward onto a business day: a Saturday or Sunday advances to the
 * following Monday, a weekday is left unchanged. Applied to the <em>effective</em> registration
 * date — whether supplied on the request or defaulted to the server date — so that the persisted
 * {@code registrationDate} always falls on a business day and every value derived from it (the
 * membership number's year segment in {@link AssignMembershipNumber}, the per-day create-limit
 * bucket in {@link CheckOwnerDailyLimit}) is computed from the same adjusted date.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /** The given date if it is a weekday, otherwise the next Monday. */
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
