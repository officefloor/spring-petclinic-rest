package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the new owner a membership tier: the first {@value #FOUNDING_LIMIT} owners ever
 * created are {@code FOUNDING}, all later owners are {@code STANDARD}. The new owner's
 * ordinal position is one more than the number of owners that existed beforehand.
 */
public class AssignMembershipTier {

    static final int FOUNDING_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int precedingOwnerCount = ownerRepository.findAll().size();
        int ordinal = precedingOwnerCount + 1;
        owner.setMembershipTier(ordinal <= FOUNDING_LIMIT ? "FOUNDING" : "STANDARD");
    }
}
