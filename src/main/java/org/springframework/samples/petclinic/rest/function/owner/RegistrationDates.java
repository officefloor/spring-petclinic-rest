package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Registration-date business rules shared by the owner-create pipeline. A registration
 * date must fall on a business day: when it lands on a Saturday or Sunday it rolls forward
 * to the next Monday. This applies to the effective date whether it was supplied on the
 * request or defaulted to the server's current date, so both the step that stores the date
 * ({@link ResolveRegistrationDate}) and the daily-limit guard ({@link EnsureDailyLimit})
 * derive the same adjusted day from here.
 */
final class RegistrationDates {

    private RegistrationDates() {
    }

    /** Roll a weekend date forward to the following Monday; a weekday is returned unchanged. */
    static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    /**
     * The effective registration date: the supplied date, or the server's current date when
     * none was supplied, in either case rolled forward onto a business day.
     */
    static LocalDate effective(LocalDate supplied) {
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }
}
