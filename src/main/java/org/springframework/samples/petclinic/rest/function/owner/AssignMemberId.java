package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.MemberId;
import org.springframework.samples.petclinic.util.Postcodes;

/**
 * Assigns the owner's member id before it is saved. The id is the region-and-hash identity
 * {@code <REGION><FY><HASH8><CHK>} (see {@link MemberId}): {@code REGION} is the region derived
 * from the owner's postcode, {@code FY} the two-digit fiscal year of the registration date,
 * {@code HASH8} the first eight upper-case hex characters of the SHA-256 digest over the
 * normalized telephone and last name, and {@code CHK} a Luhn check digit over the preceding
 * segments. Should that id collide with an existing owner's, it is
 * {@link MemberId#deduplicate de-duplicated} with a {@code -<n>} suffix so distinct owners always
 * receive distinct ids. Runs after {@link NormalizeOwnerTelephone} has put the telephone in E.164
 * form, {@link BuildOwner} has produced the entity and {@link ResolveRegistrationDate} has set the
 * registration date, so the hash sees the normalized value and the fiscal year is known; mutates
 * the built owner in place.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Postcodes.regionCode(owner.getPostcode());
        String memberId = MemberId.of(region, owner.getRegistrationDate(), owner.getTelephone(),
                owner.getLastName());
        owner.setMemberId(MemberId.deduplicate(memberId, existingIds(ownerRepository)));
    }

    private static Set<String> existingIds(OwnerRepository ownerRepository) {
        Set<String> ids = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                ids.add(existing.getMemberId());
            }
        }
        return ids;
    }
}
