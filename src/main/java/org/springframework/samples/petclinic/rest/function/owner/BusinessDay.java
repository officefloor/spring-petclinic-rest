package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Set;

/**
 * Registration-date business-day rule. The effective registration date — whether it was
 * supplied in the request or defaulted to the server's current date — must fall on a
 * business day: a Saturday, Sunday or listed public holiday rolls forward to the next
 * non-holiday weekday. Applied wherever the registration date is derived
 * ({@link BuildOwner}) or compared ({@link ValidateDailyOwnerLimit}) so both agree on the
 * same adjusted day.
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
     * Roll a date forward to the next business day: weekends and listed public holidays are
     * skipped one day at a time until a non-holiday weekday is reached; such a day is
     * returned unchanged.
     */
    static LocalDate rollForward(LocalDate date) {
        while (isNonBusinessDay(date)) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static boolean isNonBusinessDay(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> true;
            default -> HOLIDAYS.contains(date);
        };
    }

    /**
     * The effective, business-day-adjusted registration date: the supplied date when
     * present, otherwise the server's current date, rolled forward off any weekend.
     */
    static LocalDate effectiveRegistrationDate(LocalDate supplied) {
        return rollForward(supplied != null ? supplied : LocalDate.now());
    }
}
