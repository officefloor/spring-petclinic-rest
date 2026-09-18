package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: assigns the new owner's {@code memberId}. The id is
 * {@code <REGION><FY><HASH8><CHK>} — the region derived from the owner's postcode (see
 * {@link Locality}), the two-digit fiscal year of the registration date, a stable hash of the
 * owner's normalized telephone and last name, and a Luhn check digit. It runs after the
 * telephone has been normalized (so the hash covers the E.164 value) and the registration date
 * has been resolved (so the fiscal-year segment is final), and is persisted with the row and
 * returned on later reads.
 *
 * <p>Should the formatted id collide with an existing owner's {@code memberId}, it is
 * de-duplicated by appending {@code -<n>} (see {@link MemberId#deduplicate}).
 *
 * @see MemberId for the {@code <REGION><FY><HASH8><CHK>} format.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.region(owner.getCity(), owner.getPostcode());
        String memberId = MemberId.format(region, owner.getRegistrationDate(),
                owner.getTelephone(), owner.getLastName());
        Set<String> inUse = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                inUse.add(existing.getMemberId());
            }
        }
        owner.setMemberId(MemberId.deduplicate(memberId, inUse::contains));
    }
}
