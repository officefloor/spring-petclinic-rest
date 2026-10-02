package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Registration-date business-day rule. The effective registration date — whether it was
 * supplied in the request or defaulted to the server's current date — must fall on a
 * business day: a Saturday or Sunday rolls forward to the following Monday. Applied
 * wherever the registration date is derived ({@link BuildOwner}) or compared
 * ({@link ValidateDailyOwnerLimit}) so both agree on the same adjusted day.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /** Roll a weekend date forward to the next Monday; a weekday is returned unchanged. */
    static LocalDate rollForward(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY -> date.plusDays(2);
            case SUNDAY -> date.plusDays(1);
            default -> date;
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
