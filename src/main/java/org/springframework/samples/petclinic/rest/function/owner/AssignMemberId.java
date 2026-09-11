package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where
 * REGION is the region code derived from the postcode, FY the two-digit fiscal year of
 * the registrationDate, HASH8 the first 8 upper-case hex characters of SHA-256 over
 * (normalizedTelephone + lastName) and CHK a Luhn check digit over the preceding digits
 * — see {@link MemberIds}. The base identity is a pure function of the owner's own fields.
 *
 * <p>When the computed memberId collides with an existing owner's {@code memberId}, a
 * {@code -<n>} suffix is appended with the smallest {@code n} of 2 or more that makes it
 * unique, so distinct owners always end up with distinct memberIds.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }
        String base = MemberIds.forOwner(owner);
        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }
}
