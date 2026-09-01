package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Rolls a registration date forward onto a business day: a Saturday, Sunday or listed public
 * holiday rolls to the next non-holiday weekday, any other weekday is left unchanged. Applied to
 * the effective registration date (supplied or defaulted) so every value derived from it — the
 * membership number's year, the daily create-limit bucket — sees the same adjusted day.
 */
public final class BusinessDay {

    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 26), LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25), LocalDate.of(2026, 12, 28));

    private BusinessDay() {
    }

    public static LocalDate roll(LocalDate date) {
        LocalDate rolled = date;
        while (rolled.getDayOfWeek() == DayOfWeek.SATURDAY
                || rolled.getDayOfWeek() == DayOfWeek.SUNDAY || HOLIDAYS.contains(rolled)) {
            rolled = rolled.plusDays(1);
        }
        return rolled;
    }
}
