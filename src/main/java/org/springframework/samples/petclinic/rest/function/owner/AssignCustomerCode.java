package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <LAST3>-<NNNN>} where
 * LAST3 is the upper-cased first three letters of the last name and NNNN is a global
 * 4-digit zero-padded sequence equal to one more than the current number of owners
 * (e.g. {@code SMI-0007}).
 *
 * <p>Runs after {@code BuildOwner} (so the owner exists) but before {@code SaveOwner}
 * (so the owner being created is not yet counted), mutating the owner in place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = ownerRepository.findAll().size() + 1;
        String lastName = owner.getLastName();
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase();
        owner.setCustomerCode(String.format("%s-%04d", last3, sequence));
    }
}
