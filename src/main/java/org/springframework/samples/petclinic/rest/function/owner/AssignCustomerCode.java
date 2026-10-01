package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs after {@link BuildOwner} and before {@link SaveOwner} on {@code POST /api/owners}. Assigns the
 * new owner's {@code customerCode} formatted {@code <LAST3>-<NNNN>}, where LAST3 is the upper-cased
 * first three letters of the owner's lastName and NNNN is a global 4-digit zero-padded sequence equal
 * to one more than the current number of owners (e.g. {@code SMI-0007}). The owner is still unsaved, so
 * the count excludes it and the sequence advances by one per create.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
