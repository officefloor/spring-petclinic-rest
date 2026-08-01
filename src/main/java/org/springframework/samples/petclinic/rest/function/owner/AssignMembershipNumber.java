package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner a sequential membership number equal to one more than the
 * number of owners that already exist. Runs before the owner is saved, so the
 * current count excludes this owner.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int currentOwnerCount = ownerRepository.findAll().size();
        owner.setMembershipNumber(currentOwnerCount + 1);
    }
}
