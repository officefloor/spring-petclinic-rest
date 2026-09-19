package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Defaults the create-owner registration date: when the request omits it, stamps the
 * server's current date so every new owner has one. A supplied date is left untouched.
 * Mutates the shared request in place so {@link BuildOwner} persists the value.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val OwnerFieldsDto request) {
        if (request.getRegistrationDate() == null) {
            request.setRegistrationDate(LocalDate.now());
        }
    }
}
