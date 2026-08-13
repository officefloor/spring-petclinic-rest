package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day rules for owner registration. A registration date must fall on a business day:
 * when it lands on a Saturday, Sunday or a listed public holiday it rolls forward to the next
 * non-holiday weekday. Used both to adjust the effective registration date (see
 * {@link DefaultOwnerRegistrationDate}) and to bucket the daily create-limit (see
 * {@link CheckDailyOwnerLimit}) so both share one definition.
 */
final class BusinessDay {

    /** Fixed public holidays the business-day roll skips. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    /**
     * Roll {@code date} forward over Saturdays, Sundays and listed public holidays to the next
     * non-holiday business day; a weekday that is not a holiday is unchanged.
     */
    static LocalDate rollForward(LocalDate date) {
        while (isWeekend(date) || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
