package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * The registration date must fall on a business day. Computes the EFFECTIVE registration date for a
 * create: the date supplied on the request, or the server's current date when none was supplied,
 * rolled forward to the next business day when it lands on a Saturday, Sunday or public holiday.
 * Every value derived from the registration date (the membership number's year segment, the daily
 * create-limit's day) uses this adjusted date.
 */
public final class BusinessDay {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"), LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /**
     * The effective registration date for a create: {@code supplied} when present, otherwise the
     * server's current date, rolled forward off a weekend to the next Monday.
     */
    public static LocalDate effectiveRegistrationDate(LocalDate supplied) {
        return rollForward(supplied != null ? supplied : LocalDate.now());
    }

    /**
     * Rolls a Saturday, Sunday or public holiday forward to the next non-holiday business day; a
     * business day that is not a holiday is returned unchanged.
     */
    public static LocalDate rollForward(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }
}
