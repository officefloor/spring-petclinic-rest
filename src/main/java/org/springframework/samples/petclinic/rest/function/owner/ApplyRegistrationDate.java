package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Sets the owner's registration date to a business day. The effective date is the one supplied in
 * the create request, or the server's current date when none was supplied; if that date falls on a
 * Saturday or Sunday it is rolled forward to the following Monday. Everything derived from the
 * registration date (such as the membership number's year segment) sees this adjusted value.
 */
public class ApplyRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate effective = owner.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        owner.setRegistrationDate(BusinessDays.rollForward(effective));
    }
}
