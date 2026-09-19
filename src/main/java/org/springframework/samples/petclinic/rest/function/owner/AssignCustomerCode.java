package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's customer code before it is saved. The code is formatted
 * {@code <LAST3>-<NNNN>}, where {@code LAST3} is the upper-cased first three letters of
 * the last name and {@code NNNN} is a global 4-digit zero-padded sequence equal to one
 * more than the current number of owners (e.g. {@code SMI-0007}). Runs after
 * {@link BuildOwner} has produced the entity and before {@link SaveOwner} persists it,
 * mutating the built owner in place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(format(owner.getLastName(), sequence));
    }

    private static String format(String lastName, int sequence) {
        String prefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        return String.format("%s-%04d", prefix, sequence);
    }
}
