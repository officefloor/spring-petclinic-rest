package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a sequential membership number to a newly created owner, equal to one
 * more than the number of owners that already exist. The owner being created has
 * not yet been saved, so it is not counted.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setMembershipNumber(ownerRepository.findAll().size() + 1);
    }
}
