package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's {@code customerCode} before it is saved: the global 4-digit
 * sequence is one more than the current number of owners, so codes run consecutively
 * across all owners regardless of last name.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = ownerRepository.findAll().size() + 1;
        owner.setCustomerCode(OwnerCustomerCodes.format(owner.getLastName(), sequence));
    }
}
