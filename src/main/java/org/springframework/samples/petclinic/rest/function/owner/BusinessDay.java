package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Single source of truth for the business-day rule on an owner's registration date: the
 * effective registration date must fall on a business day, so a Saturday or Sunday rolls
 * forward to the following Monday. A date that is already a weekday is returned unchanged.
 *
 * <p>Centralised here so every site agrees on one definition: {@link BuildOwner} stores the
 * adjusted date on the owner (and everything derived from it, such as the membership number's
 * year segment in {@link AssignMembershipNumber}, then uses the adjusted value), while
 * {@link CheckDailyOwnerLimit} counts existing owners against the same adjusted business day.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /**
     * Rolls {@code date} forward to the next business day: a Saturday or Sunday advances to the
     * following Monday; a weekday is returned unchanged.
     */
    static LocalDate rollForward(LocalDate date) {
        LocalDate rolled = date;
        while (rolled.getDayOfWeek() == DayOfWeek.SATURDAY || rolled.getDayOfWeek() == DayOfWeek.SUNDAY) {
            rolled = rolled.plusDays(1);
        }
        return rolled;
    }
}
