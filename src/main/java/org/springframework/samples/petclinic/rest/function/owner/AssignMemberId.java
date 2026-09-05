package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, the single unified identity formatted
 * {@code <REGION><FY><HASH8><CHK>} where REGION is the region code derived from the postcode,
 * FY is the two-digit fiscal year of the registration date, HASH8 is the first 8 upper-case
 * hex characters of SHA-256 over {@code normalizedTelephone + lastName}, and CHK is a single
 * Luhn check digit over the digits of {@code <REGION><FY><HASH8>} (e.g. {@code NSW261A2B3C4D7}).
 * There is no sequence number; see {@link OwnerMemberId} for the exact derivation.
 *
 * <p>De-duplication: when the computed id already belongs to another owner, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique (so a first
 * collision yields {@code <base>-2}, the next {@code <base>-3}, and so on). The owner ends up
 * with the de-duplicated {@code memberId}.
 *
 * <p>Runs after {@code BuildOwner} (so the owner exists, with its telephone already
 * normalized to E.164 and its registration date rolled to a business day) but before
 * {@code SaveOwner} (so the owner being created is not yet persisted and cannot collide with
 * itself), mutating the owner in place.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = OwnerMemberId.of(owner.getPostcode(), owner.getCity(), owner.getTelephone(),
                owner.getLastName(), owner.getRegistrationDate());

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(other.getId())) {
                continue; // never de-duplicate against the owner being created
            }
            if (other.isDeleted()) {
                continue; // a soft-deleted owner's member id is free to reuse
            }
            String id = other.getMemberId();
            if (id != null) {
                existing.add(id);
            }
        }

        String id = base;
        for (int n = 2; existing.contains(id); n++) {
            id = base + "-" + n;
        }
        owner.setMemberId(id);
    }
}
