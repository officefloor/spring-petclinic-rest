package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * De-duplicates the assigned {@code customerCode}. If another owner already holds the
 * computed code, appends {@code -<n>} with the smallest {@code n >= 2} that is unique.
 * The owner is not yet saved, so its own code is absent from the store.
 */
public class DeduplicateOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            taken.add(other.getCustomerCode());
        }
        String base = owner.getCustomerCode();
        String code = base;
        for (int n = 2; taken.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }
}
