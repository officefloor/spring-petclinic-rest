package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that fills in the registration date when the request omits one.
 * Runs after {@link BuildOwner}: if the built owner has no registration date, it is set to the
 * server's current date. A date supplied in the request body is left untouched. The stored
 * {@code registrationDate} is returned in ISO format 'YYYY-MM-DD'.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
    }
}
