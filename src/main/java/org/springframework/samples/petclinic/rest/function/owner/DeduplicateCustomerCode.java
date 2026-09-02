package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Ensures the owner's {@code customerCode} is unique. Runs after {@link AssignCustomerCode} and
 * before the owner is saved, so {@code findAll()} sees only the pre-existing owners. When the
 * computed code already belongs to another owner, appends {@code -<n>} with the smallest
 * {@code n >= 2} that makes it unique.
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
