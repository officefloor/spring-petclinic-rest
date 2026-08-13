package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Defaults a new owner's registration date to the server's current date when the request omits it.
 * A supplied date is left untouched. Runs after {@link BuildOwner} maps the request and before
 * {@link SaveOwner} persists it.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
    }
}
