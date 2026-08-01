package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

public class AssignMembershipTier {

    /** The number of owners that qualify for the founding membership tier. */
    private static final int FOUNDING_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int currentOwnerCount = ownerRepository.findAll().size();
        // This owner's position is one more than the number that already exist.
        int position = currentOwnerCount + 1;
        owner.setMembershipTier(position <= FOUNDING_LIMIT ? "FOUNDING" : "STANDARD");
    }
}
