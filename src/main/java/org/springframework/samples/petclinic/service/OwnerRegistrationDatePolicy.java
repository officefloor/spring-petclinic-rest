package org.springframework.samples.petclinic.service;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

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
        LocalDate effective = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        owner.setRegistrationDate(rollToBusinessDay(effective));
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
}
