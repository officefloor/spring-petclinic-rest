package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in the create-owner pipeline after {@link BuildOwner}. The registration date is optional on
 * create: when the request supplied one it is carried through untouched, but when it is absent this
 * step defaults it to the server's current date, mutating the built owner in place so later steps
 * store and respond with the resolved value (formatted as ISO 'YYYY-MM-DD').
 */
public class DefaultOwnerRegistrationDate {

    public void service(@Val Owner owner) {
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
    }
}
