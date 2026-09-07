package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Makes the computed {@link CustomerCode customer code} unique before the owner is saved. The code
 * is a pure function of region, telephone and last name, so two distinct owners can in principle
 * derive the same value; when the code collides with an existing owner's customer code, the
 * smallest {@code n} of 2 or more is appended as {@code '-<n>'} (see
 * {@link CustomerCode#dedupe(String, Set)}). Runs after the code has been stamped by
 * {@link BuildOwner} and before {@link SaveOwner}, mutating the owner in place.
 */
public class DeduplicateCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getId() != null && other.getId().equals(owner.getId())) {
                continue; // same record, not a collision
            }
            if (other.getCustomerCode() != null) {
                existing.add(other.getCustomerCode());
            }
        }
        owner.setCustomerCode(CustomerCode.dedupe(owner.getCustomerCode(), existing));
    }
}
