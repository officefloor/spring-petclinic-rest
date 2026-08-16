package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Business-day rules for an owner's effective registration date.
 *
 * <p>The registration date must fall on a business day. A Saturday or Sunday rolls forward to the
 * next Monday; a weekday is kept. This applies to the effective date whether supplied in the
 * request or defaulted to the server's current date, and every value derived from the registration
 * date (membership number year, the daily create-limit day) must use the adjusted date.
 */
final class RegistrationDate {

    private RegistrationDate() {
    }

    /** Roll a Saturday or Sunday forward to the next Monday; a weekday is returned unchanged. */
    static LocalDate toBusinessDay(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (day == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
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
