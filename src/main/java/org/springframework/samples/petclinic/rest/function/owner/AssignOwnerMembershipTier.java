package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner a membership tier: the first 100 owners ever created
 * (i.e. when fewer than 100 owners already exist) are 'FOUNDING'; all later owners are
 * 'STANDARD'.
 */
public class AssignOwnerMembershipTier {

    private static final int FOUNDING_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        boolean founding = ownerRepository.findAll().size() < FOUNDING_LIMIT;
        owner.setMembershipTier(founding ? "FOUNDING" : "STANDARD");
    }
}
