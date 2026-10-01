package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} (e.g.
 * {@code NSW271A2B3C4D5}). The shape and all of its parts are owned by {@link OwnerMemberId}.
 *
 * <p>The region-and-hash portion depends only on the owner's own fields, but two distinct owners can
 * nonetheless hash to the same id. On a collision with an existing owner's {@code memberId} the id is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique (see {@link OwnerMemberId#deduplicate}), so distinct owners always keep distinct member ids.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = OwnerMemberId.memberId(owner);
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }
        owner.setMemberId(OwnerMemberId.deduplicate(base, existing));
    }
}
