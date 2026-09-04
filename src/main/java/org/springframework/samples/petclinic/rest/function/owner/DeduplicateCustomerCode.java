package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Ensures the owner's {@code customerCode} is unique. When the code computed by
 * {@link AssignCustomerCode} collides with an existing owner's {@code customerCode}, appends
 * {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique. Runs before the owner
 * is saved, so {@code findAll()} sees only the existing owners.
 */
public class DeduplicateCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            taken.add(existing.getCustomerCode());
        }
        String base = owner.getCustomerCode();
        String candidate = base;
        for (int n = 2; taken.contains(candidate); n++) {
            candidate = base + "-" + n;
        }
        owner.setCustomerCode(candidate);
    }
}
