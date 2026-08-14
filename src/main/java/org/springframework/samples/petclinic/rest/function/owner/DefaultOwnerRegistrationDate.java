package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that stores the effective registration date on the built owner.
 * The date is resolved once by {@link ResolveRegistrationDate} (supplied-or-default, rolled forward
 * off weekends to the next business day) and published as a variable; this step applies that single
 * adjusted value to the owner, overwriting any raw weekend date the request supplied. The stored
 * {@code registrationDate} is returned in ISO format 'YYYY-MM-DD'.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val Owner owner, @Val LocalDate registrationDate) {
        owner.setRegistrationDate(registrationDate);
    }
}
