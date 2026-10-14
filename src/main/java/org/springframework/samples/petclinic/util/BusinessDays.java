package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Business-day calculations for dates such as an owner's registration date.
 */
public final class BusinessDays {

    /** Fixed public holidays that are never business days. */
    static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 1, 26),
        LocalDate.of(2026, 4, 25),
        LocalDate.of(2026, 12, 25),
        LocalDate.of(2026, 12, 28));

    private BusinessDays() {
    }

    /**
     * Rolls a date forward past Saturdays, Sundays and public holidays to the next business day;
     * a date that is already a business day is returned unchanged.
     *
     * @param date the date to adjust
     * @return the business day on or after {@code date}, or {@code null} if {@code date} is {@code null}
     */
    public static LocalDate rollForward(LocalDate date) {
        if (date == null) {
            return null;
        }
        LocalDate result = date;
        while (!isBusinessDay(result)) {
            result = result.plusDays(1);
        }
        return result;
    }

    /**
     * @param date the date to check
     * @return {@code true} if {@code date} is neither a weekend day nor a public holiday
     */
    public static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
            && !PUBLIC_HOLIDAYS.contains(date);
    }
}
