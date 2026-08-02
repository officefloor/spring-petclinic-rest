package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner a sequential membership number equal to one more than
 * the number of owners that currently exist. Runs before the owner is saved, so the
 * count reflects only the owners that existed beforehand.
 */
public class AssignMembershipNumber {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int currentOwnerCount = ownerRepository.findAll().size();
        owner.setMembershipNumber(currentOwnerCount + 1);
    }
}
