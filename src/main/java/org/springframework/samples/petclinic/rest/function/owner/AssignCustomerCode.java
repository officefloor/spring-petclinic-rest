package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's customer code before it is saved. The sequence is global: one
 * more than the current number of owners.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int sequence = ownerRepository.count() + 1;
        owner.setCustomerCode(CustomerCode.format(owner.getLastName(), sequence));
    }
}
