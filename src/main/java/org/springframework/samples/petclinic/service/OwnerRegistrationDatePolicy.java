package org.springframework.samples.petclinic.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: an owner's effective registration date must fall on a business day. The effective
 * date is the one supplied on the request, or the server's current date when none was supplied; a
 * Saturday or Sunday rolls forward to the following Monday. Kept as a small, self-contained unit so
 * the rule can be applied from the create flow before any value derived from the registration date
 * (membership number, per-day create limit) is computed.
 */
public final class OwnerRegistrationDatePolicy {

    private OwnerRegistrationDatePolicy() {
    }

    /** Set the owner's registrationDate to its effective business-day value. */
    public static void assignBusinessDayRegistrationDate(Owner owner) {
        LocalDate supplied = owner.getRegistrationDate();
        rejectFutureDate(supplied);
        LocalDate effective = supplied != null ? supplied : LocalDate.now();
        owner.setRegistrationDate(rollToBusinessDay(effective));
    }

    /** Reject a supplied registration date that is later than the server's current date. */
    private static void rejectFutureDate(LocalDate supplied) {
        if (supplied != null && supplied.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException();
        }
    }

    private static LocalDate rollToBusinessDay(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return date.plusDays(2);
        }
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.plusDays(1);
        }
        return date;
    }

    /** Thrown when a supplied registration date is later than the server's current date. */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class FutureRegistrationDateException extends RuntimeException {
    }
}
