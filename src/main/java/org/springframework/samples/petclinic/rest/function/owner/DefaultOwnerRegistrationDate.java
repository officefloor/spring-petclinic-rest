package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Defaults the owner {@code registrationDate} on create: an absent (null) value is set to the server's
 * current date, while a supplied value is left as-is. The value is written back onto the (shared)
 * {@link OwnerFieldsDto} so {@link BuildOwner} stores it and {@code GET /owners/{id}} later returns it
 * in ISO 'YYYY-MM-DD' format. Only wired into the create pipeline, so updates never re-stamp the date.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto owner) {
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
    }
}
