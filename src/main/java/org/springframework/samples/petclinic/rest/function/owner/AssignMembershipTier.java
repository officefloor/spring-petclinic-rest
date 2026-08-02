package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a membership tier to a newly built owner: the first 100 owners ever
 * created (those with no more than 100 owners existing before them) are
 * 'FOUNDING'; all later owners are 'STANDARD'.
 */
public class AssignMembershipTier {

    private static final int FOUNDING_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int existing = ownerRepository.findAll().size();
        owner.setMembershipTier(existing < FOUNDING_LIMIT ? "FOUNDING" : "STANDARD");
    }
}
