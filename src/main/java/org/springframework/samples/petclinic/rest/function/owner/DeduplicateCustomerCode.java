package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.CustomerCode;

/**
 * Guarantees the assigned customerCode is unique across owners. When the code computed by
 * {@link AssignCustomerCode} already belongs to an existing owner, it appends {@code -<n>} with the
 * smallest {@code n} of 2 or more that makes it unique (see {@link CustomerCode#dedupe}). Runs before
 * {@link SaveOwner}, so the owner being created is not yet persisted and cannot collide with itself.
 * Mutates the built {@link Owner} in place.
 */
public class DeduplicateCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getCustomerCode() != null) {
                existing.add(other.getCustomerCode());
            }
        }
        owner.setCustomerCode(CustomerCode.dedupe(owner.getCustomerCode(), existing));
    }
}
