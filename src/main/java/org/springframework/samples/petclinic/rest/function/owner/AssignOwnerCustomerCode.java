package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's {@code customerCode} before it is saved. The code is
 * {@code <REGION>-<HASH8>} (see {@link OwnerCustomerCodes}): the region derived from the owner's
 * postcode and a hash of their telephone and last name, so it is a stable function of the owner's
 * own identity rather than a per-city sequence. When that code collides with an existing owner's,
 * it is de-duplicated by appending {@code -<n>} (smallest {@code n} of 2 or more that is unique).
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = OwnerCustomerCodes.forOwner(owner);
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCustomerCode() != null) {
                taken.add(existing.getCustomerCode());
            }
        }
        owner.setCustomerCode(OwnerCustomerCodes.deduplicate(base, taken));
    }
}
