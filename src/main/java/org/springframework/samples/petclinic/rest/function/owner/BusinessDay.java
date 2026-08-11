package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Business-day rules for owner registration. A registration date must fall on a business day:
 * a Saturday or Sunday rolls forward to the next Monday. This applies to the effective
 * registration date, whether supplied in the request or defaulted to the server date.
 */
final class BusinessDay {

    private BusinessDay() {
    }

    /** Rolls a weekend date forward to the next Monday; a weekday is returned unchanged. */
    static LocalDate rollForward(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (dow == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
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
