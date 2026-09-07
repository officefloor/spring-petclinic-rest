package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Makes the computed {@link MemberId member id} unique before the owner is saved. The id is a pure
 * function of region, fiscal year, telephone and last name, so two distinct owners can in principle
 * derive the same value; when the id collides with an existing owner's member id, the smallest
 * {@code n} of 2 or more is appended as {@code '-<n>'} (see {@link MemberId#dedupe(String, Set)}).
 * Runs after the id has been stamped by {@link BuildOwner} and before {@link SaveOwner}, mutating
 * the owner in place.
 */
public class DeduplicateMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getId() != null && other.getId().equals(owner.getId())) {
                continue; // same record, not a collision
            }
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }
        owner.setMemberId(MemberId.dedupe(owner.getMemberId(), existing));
    }
}
