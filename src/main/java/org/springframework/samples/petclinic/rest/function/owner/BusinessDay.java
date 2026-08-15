package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Rolls a registration date forward onto a business day: a Saturday, Sunday or listed public
 * holiday advances to the next non-holiday weekday, a plain weekday is left unchanged. Applied to
 * the <em>effective</em> registration date — whether supplied on the request or defaulted to the
 * server date — so that the persisted {@code registrationDate} always falls on a business day and
 * every value derived from it (the membership number's year segment in
 * {@link AssignMembershipNumber}, the per-day create-limit bucket in {@link CheckOwnerDailyLimit})
 * is computed from the same adjusted date.
 */
final class BusinessDay {

    /** Fixed public-holiday calendar; a date on this list is not a business day. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDay() {
    }

    /** The given date if it is a business day, otherwise the next non-weekend, non-holiday date. */
    static LocalDate rollForward(LocalDate date) {
        while (!isBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY && !PUBLIC_HOLIDAYS.contains(date);
    }
}
