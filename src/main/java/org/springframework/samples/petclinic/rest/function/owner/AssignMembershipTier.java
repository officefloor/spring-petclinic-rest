package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class AssignMembershipTier {

    /** The first 100 owners ever created are FOUNDING; all later owners are STANDARD. */
    private static final int FOUNDING_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int existingOwnerCount = ownerRepository.findAll().size();
        owner.setMembershipTier(existingOwnerCount < FOUNDING_LIMIT ? "FOUNDING" : "STANDARD");
    }
}
