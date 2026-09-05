package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, the region-and-hash identity formatted
 * {@code <REGION>-<HASH8>} where REGION is the region code derived from the postcode and
 * HASH8 is the first 8 upper-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName} (e.g. {@code NSW-1A2B3C4D}). There is no sequence
 * number; see {@link OwnerCustomerCode} for the exact derivation.
 *
 * <p>De-duplication: when the computed code already belongs to another owner, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique (so a first
 * collision yields {@code <base>-2}, the next {@code <base>-3}, and so on). The owner ends up
 * with the de-duplicated {@code customerCode}.
 *
 * <p>Runs after {@code BuildOwner} (so the owner exists, with its telephone already
 * normalized to E.164) but before {@code SaveOwner} (so the owner being created is not yet
 * persisted and cannot collide with itself), mutating the owner in place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = OwnerCustomerCode.of(
                owner.getPostcode(), owner.getCity(), owner.getTelephone(), owner.getLastName());

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(other.getId())) {
                continue; // never de-duplicate against the owner being created
            }
            if (other.isDeleted()) {
                continue; // a soft-deleted owner's identity code is free to reuse
            }
            String code = other.getCustomerCode();
            if (code != null) {
                existing.add(code);
            }
        }

        String code = base;
        for (int n = 2; existing.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }
}
