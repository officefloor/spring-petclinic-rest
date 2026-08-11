package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Business-day rules for owner registration. A registration date must fall on a business day:
 * a Saturday, Sunday or listed public holiday rolls forward to the next non-holiday weekday.
 * This applies to the effective registration date, whether supplied in the request or defaulted
 * to the server date.
 */
final class BusinessDay {

    /** Fixed public-holiday calendar; a date landing on one of these rolls forward. */
    private static final Set<LocalDate> PUBLIC_HOLIDAYS = Set.of(
            LocalDate.parse("2026-01-01"),
            LocalDate.parse("2026-01-26"),
            LocalDate.parse("2026-04-25"),
            LocalDate.parse("2026-12-25"),
            LocalDate.parse("2026-12-28"));

    private BusinessDay() {
    }

    private static boolean isBusinessDay(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY && !PUBLIC_HOLIDAYS.contains(date);
    }

    /**
     * Rolls a date forward to the next business day: weekends and listed public holidays are
     * skipped one day at a time until a non-holiday weekday is reached; a business day is
     * returned unchanged.
     */
    static LocalDate rollForward(LocalDate date) {
        LocalDate rolled = date;
        while (!isBusinessDay(rolled)) {
            rolled = rolled.plusDays(1);
        }
        return rolled;
    }

    /**
     * The adjusted effective registration date for a create request: the supplied date, or the
     * server date when omitted, rolled forward off any weekend.
     */
    static LocalDate effective(OwnerFieldsDto request) {
        LocalDate supplied = request.getRegistrationDate();
        return rollForward(supplied != null ? supplied : LocalDate.now());
    }
}
