package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners}: resolves the new owner's effective registration date and
 * ensures it falls on a business day. The effective date is the one supplied in the request,
 * or the server's current date when the request supplied none; either way, a weekend date is
 * rolled forward to the following Monday (see {@link BusinessDay}). Runs on the built owner so
 * both the persisted row and the response — and every value derived from the date, such as the
 * membership number's year segment — carry the adjusted value.
 */
public class ResolveOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        LocalDate effective = owner.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        owner.setRegistrationDate(BusinessDay.adjust(effective));
    }
}
