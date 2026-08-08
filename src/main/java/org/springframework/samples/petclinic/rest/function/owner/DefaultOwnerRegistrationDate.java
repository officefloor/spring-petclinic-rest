package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Ensures a newly created owner's registration date falls on a business day.
 *
 * <p>The effective registration date is the one supplied in the request body, or the
 * server's current date when none was supplied. When that effective date is a Saturday
 * or Sunday it is rolled forward to the following Monday and stored as the
 * {@code registrationDate}. Because later steps read this same entity, every value
 * derived from the registration date (the membership number's year segment, the
 * daily create-limit) sees the adjusted business day.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate effective = owner.getRegistrationDate() != null
                ? owner.getRegistrationDate() : LocalDate.now();
        owner.setRegistrationDate(toBusinessDay(effective));
    }

    /** Rolls a weekend date forward to the next Monday; business days are unchanged. */
    private static LocalDate toBusinessDay(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            date = date.plusDays(1);
        }
        return date;
    }
}
