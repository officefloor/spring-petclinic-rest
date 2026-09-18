package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Defaults a newly built owner's {@code registrationDate} when the create body supplied
 * none: the server's current date is stored in place on the built entity, so it is
 * persisted and returned as ISO 'YYYY-MM-DD'. A date supplied in the body is left
 * untouched. Runs after {@link BuildOwner} and before the owner is saved.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
    }
}
