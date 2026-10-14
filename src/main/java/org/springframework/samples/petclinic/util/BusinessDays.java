package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Business-day calculations for dates such as an owner's registration date.
 */
public final class BusinessDays {

    private BusinessDays() {
    }

    /**
     * Rolls a Saturday or Sunday forward to the following Monday; any other date is returned unchanged.
     *
     * @param date the date to adjust
     * @return the business day on or after {@code date}, or {@code null} if {@code date} is {@code null}
     */
    public static LocalDate rollForward(LocalDate date) {
        if (date == null) {
            return null;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return date.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        return date;
    }
}
