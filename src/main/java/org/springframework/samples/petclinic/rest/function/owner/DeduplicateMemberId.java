package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.MemberId;

/**
 * Guarantees the assigned memberId is unique across owners. When the id computed by
 * {@link AssignMemberId} already belongs to an existing owner, it appends {@code -<n>} with the
 * smallest {@code n} of 2 or more that makes it unique (see {@link MemberId#dedupe}). Runs before
 * {@link SaveOwner}, so the owner being created is not yet persisted and cannot collide with itself.
 * Mutates the built {@link Owner} in place.
 */
public class DeduplicateMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }
        owner.setMemberId(MemberId.dedupe(owner.getMemberId(), existing));
    }
}
