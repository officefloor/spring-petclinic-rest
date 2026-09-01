package org.springframework.samples.petclinic.rest.controller.v1;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business-day rule for owner registration dates: the effective registration
 * date defaults to the server's current date when absent and, when it lands on
 * a weekend, rolls forward to the following Monday.
 */
final class BusinessDays {

    private BusinessDays() {
    }

    /** The owner's effective registration date, defaulted and rolled onto a business day. */
    static LocalDate effective(Owner owner) {
        LocalDate date = owner.getRegistrationDate() == null ? LocalDate.now() : owner.getRegistrationDate();
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
