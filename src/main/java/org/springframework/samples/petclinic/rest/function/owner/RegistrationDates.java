package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Registration-date business rules shared by the owner-create pipeline. A registration
 * date must fall on a business day: when it lands on a Saturday, a Sunday or a listed
 * public holiday it rolls forward to the next non-holiday weekday. This applies to the
 * effective date whether it was supplied on the request or defaulted to the server's
 * current date, so both the step that stores the date ({@link ResolveRegistrationDate})
 * and the daily-limit guard ({@link EnsureDailyLimit}) derive the same adjusted day from
 * here.
 */
final class RegistrationDates {

    /** Fixed public holidays skipped by the business-day roll. */
    private static final Set<LocalDate> HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"), LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"), LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private RegistrationDates() {
    }

    /** Roll a weekend or public-holiday date forward to the next business day; a plain
     *  weekday is returned unchanged. */
    static LocalDate toBusinessDay(LocalDate date) {
        while (isWeekend(date) || HOLIDAYS.contains(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    /**
     * The effective registration date: the supplied date, or the server's current date when
     * none was supplied, in either case rolled forward onto a business day.
     */
    static LocalDate effective(LocalDate supplied) {
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }
}
