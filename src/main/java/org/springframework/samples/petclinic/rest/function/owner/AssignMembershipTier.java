package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a membership tier to a newly created owner: the first 100 owners ever
 * created are 'FOUNDING' and all later owners are 'STANDARD'. The owner being
 * created has not yet been saved, so it is not counted; it is the
 * (existing + 1)th owner ever created.
 */
public class AssignMembershipTier {

    static final int FOUNDING_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long everCreated = ownerRepository.findAll().size() + 1;
        owner.setMembershipTier(everCreated <= FOUNDING_LIMIT ? "FOUNDING" : "STANDARD");
    }
}
