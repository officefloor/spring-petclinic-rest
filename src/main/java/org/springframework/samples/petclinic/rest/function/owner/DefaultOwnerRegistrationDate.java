package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners}: when the request supplied no registration date, sets
 * it to the server's current date. A date supplied in the body is left untouched. Runs on
 * the built owner so both the persisted row and the response carry the value.
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
    }
}
