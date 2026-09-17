package org.springframework.samples.petclinic.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day calendar rules. A business day is any weekday; Saturday and Sunday are not. Pure
 * date arithmetic with no dependency on other state, so it is a shared helper rather than a step.
 */
public final class BusinessDay {

    private BusinessDay() {
    }

    /** Whether {@code date} is a business day (Monday to Friday). */
    public static boolean isBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    /**
     * {@code date} itself when it already falls on a business day, otherwise the next business day:
     * a Saturday or Sunday rolls forward to the following Monday.
     */
    public static LocalDate onOrAfter(LocalDate date) {
        LocalDate result = date;
        while (!isBusinessDay(result)) {
            result = result.plusDays(1);
        }
        return result;
    }
}
