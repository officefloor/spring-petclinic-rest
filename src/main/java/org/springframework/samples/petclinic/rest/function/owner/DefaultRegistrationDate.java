package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Defaults a newly built owner's registration date to the server's current date when
 * the create request did not supply one. Runs after {@link BuildOwner} and mutates the
 * built owner in place, so a supplied date is preserved and an omitted one is stored
 * (and returned) as today.
 */
public class DefaultRegistrationDate {

    public void service(@Val Owner owner) {
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
    }
}
