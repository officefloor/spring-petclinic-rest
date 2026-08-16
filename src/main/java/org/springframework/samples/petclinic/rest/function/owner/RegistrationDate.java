package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Business-day rules for an owner's effective registration date.
 *
 * <p>The registration date must fall on a business day. A Saturday, Sunday or listed public holiday
 * rolls forward to the next non-holiday business day; an ordinary weekday is kept. This applies to
 * the effective date whether supplied in the request or defaulted to the server's current date, and
 * every value derived from the registration date (membership number year, the daily create-limit
 * day) must use the adjusted date.
 */
final class RegistrationDate {

    /** Fixed public-holiday calendar; these dates are never valid business days. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 26),
            LocalDate.of(2026, 4, 25),
            LocalDate.of(2026, 12, 25),
            LocalDate.of(2026, 12, 28));

    private RegistrationDate() {
    }

    /**
     * Roll a Saturday, Sunday or public holiday forward to the next non-holiday business day; a
     * business day is returned unchanged. Rolling repeats so a holiday adjacent to a weekend (or to
     * another holiday) still lands on a genuine business day.
     */
    static LocalDate toBusinessDay(LocalDate date) {
        LocalDate result = date;
        while (isWeekend(result) || PUBLIC_HOLIDAYS.contains(result)) {
            result = result.plusDays(1);
        }
        return result;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    /**
     * The effective, business-day-adjusted registration date for a create request: the supplied
     * date, else the server's current date, rolled forward off any weekend.
     */
    static LocalDate effective(OwnerFieldsDto request) {
        LocalDate supplied = request.getRegistrationDate();
        return toBusinessDay(supplied != null ? supplied : LocalDate.now());
    }
}
