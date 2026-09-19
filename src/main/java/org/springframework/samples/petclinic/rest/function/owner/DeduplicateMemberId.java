package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MemberId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Ensures a newly built owner's {@code memberId} does not collide with an existing owner's.
 * When it does, {@link MemberId#deduplicate} appends {@code -<n>} with the smallest {@code n >= 2}
 * that is free, and the de-duplicated id is stored back on the owner.
 *
 * <p>Runs after {@link AssignMemberId} has derived the id, so every value read back out of it (e.g.
 * the {@link Owner#getLocality() locality}) sees the unique variant.
 */
public class DeduplicateMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String unique = MemberId.deduplicate(owner.getMemberId(),
                id -> !ownerRepository.findByMemberId(id).isEmpty());
        owner.setMemberId(unique);
    }
}
