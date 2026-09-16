package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CustomerCode;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's customer code, formatted {@code <REGION>-<HASH8>} (see
 * {@link CustomerCode}): REGION derived from the postcode and HASH8 the first 8 upper-case
 * hex characters of SHA-256 over the normalized telephone plus last name. Runs after the
 * telephone has been normalized and mutates the built {@link Owner} in place. When the
 * computed code collides with an existing owner's code it is de-duplicated by appending
 * {@code -<n>} so distinct owners always receive distinct codes.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCustomerCode() != null) {
                taken.add(existing.getCustomerCode());
            }
        }
        owner.setCustomerCode(CustomerCode.deduplicate(CustomerCode.forOwner(owner), taken::contains));
    }
}
