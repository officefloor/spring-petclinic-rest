package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Business-day rules for owner registration. A registration date must fall on a business day:
 * when it lands on a Saturday or Sunday it rolls forward to the next Monday. Used both to adjust
 * the effective registration date (see {@link DefaultOwnerRegistrationDate}) and to bucket the
 * daily create-limit (see {@link CheckDailyOwnerLimit}) so both share one definition.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /** Roll {@code date} forward over Saturday/Sunday to the next Monday; a weekday is unchanged. */
    static LocalDate rollForward(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
