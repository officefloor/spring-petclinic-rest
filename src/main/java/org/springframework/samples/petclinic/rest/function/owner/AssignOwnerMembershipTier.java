package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a newly created owner a membership tier: the first 100 owners ever
 * created are {@code FOUNDING}; every owner created after that is {@code STANDARD}.
 * The owner being created is the {@code (n + 1)}-th owner when {@code n} owners
 * already exist, so it is {@code FOUNDING} while fewer than 100 owners exist.
 */
public class AssignOwnerMembershipTier {

    private static final int FOUNDING_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int existing = ownerRepository.findAll().size();
        owner.setMembershipTier(existing < FOUNDING_LIMIT ? "FOUNDING" : "STANDARD");
    }
}
