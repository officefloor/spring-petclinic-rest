package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.MemberId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's member id, formatted {@code <REGION><FY><HASH8><CHK>} (see
 * {@link MemberId}): REGION derived from the postcode, FY the two-digit fiscal year of the
 * registration date, HASH8 the first 8 upper-case hex characters of SHA-256 over the normalized
 * telephone plus last name, and CHK a Luhn check digit over the preceding digits. Runs after the
 * telephone has been normalized and the registration date resolved, and mutates the built
 * {@link Owner} in place. When the computed id collides with an existing owner's id it is
 * de-duplicated by appending {@code -<n>} so distinct owners always receive distinct ids.
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                taken.add(existing.getMemberId());
            }
        }
        owner.setMemberId(MemberId.deduplicate(MemberId.forOwner(owner), taken::contains));
    }
}
