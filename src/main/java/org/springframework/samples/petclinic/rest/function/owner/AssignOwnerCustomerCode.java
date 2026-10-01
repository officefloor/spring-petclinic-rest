package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>}, where {@code REGION}
 * is the region code derived from the postcode and {@code HASH8} is the first eight upper-case hex
 * characters of the SHA-256 of the normalized telephone concatenated with the last name (e.g.
 * {@code NSW-1A2B3C4D}). The shape and all of its parts are owned by {@link OwnerCustomerCode}.
 *
 * <p>The region-and-hash code depends only on the owner's own fields, but two distinct owners can
 * nonetheless hash to the same code. On a collision with an existing owner's {@code customerCode} the
 * code is de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes
 * it unique (see {@link OwnerCustomerCode#deduplicate}), so distinct owners always keep distinct
 * customer codes.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = OwnerCustomerCode.customerCode(owner);
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getCustomerCode() != null) {
                existing.add(other.getCustomerCode());
            }
        }
        owner.setCustomerCode(OwnerCustomerCode.deduplicate(base, existing));
    }
}
