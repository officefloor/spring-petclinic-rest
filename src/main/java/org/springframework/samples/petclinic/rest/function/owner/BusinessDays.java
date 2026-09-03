package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * The registration date must fall on a business day. A date landing on a Saturday or Sunday, or on a
 * listed public holiday, rolls forward to the next non-holiday business day; an ordinary weekday is
 * left unchanged. Applied to the effective registration date — whether supplied in the request or
 * defaulted to the server date — so every value derived from the registration date (membership
 * number year, per-day create limit) sees the adjusted date.
 */
final class BusinessDays {

    /** Fixed public-holiday calendar. A date landing on one of these rolls forward. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private BusinessDays() {
    }

    /**
     * Roll a weekend or public-holiday date forward to the next non-holiday business day; return an
     * ordinary weekday unchanged.
     */
    static LocalDate rollForward(LocalDate date) {
        LocalDate rolled = date;
        while (isWeekend(rolled) || HOLIDAYS.contains(rolled)) {
            rolled = rolled.plusDays(1);
        }
        return rolled;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
