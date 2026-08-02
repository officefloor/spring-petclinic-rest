package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner a membership tier. The first 100 owners ever created
 * are {@code FOUNDING}; all later owners are {@code STANDARD}. Runs before the owner is
 * saved, so the count reflects only the owners that existed beforehand: an owner is
 * founding when fewer than 100 owners already exist (i.e. it is one of the first 100).
 */
public class AssignMembershipTier {

    private static final int FOUNDING_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int currentOwnerCount = ownerRepository.findAll().size();
        owner.setMembershipTier(currentOwnerCount < FOUNDING_LIMIT ? "FOUNDING" : "STANDARD");
    }
}
