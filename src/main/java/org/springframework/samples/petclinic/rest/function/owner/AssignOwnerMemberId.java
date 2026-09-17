package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's {@code memberId} before it is saved. The id is
 * {@code <REGION><FY><HASH8><CHK>} (see {@link OwnerMemberIds}): the region derived from the owner's
 * postcode, the fiscal year of the registration date, a hash of their telephone and last name and a
 * Luhn check digit, so it is a stable function of the owner's own identity rather than a per-city
 * sequence. When that id collides with an existing owner's, it is de-duplicated by appending
 * {@code -<n>} (smallest {@code n} of 2 or more that is unique).
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = OwnerMemberIds.forOwner(owner);
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                taken.add(existing.getMemberId());
            }
        }
        owner.setMemberId(OwnerMemberIds.deduplicate(base, taken));
    }
}
