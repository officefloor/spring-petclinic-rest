package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * De-duplicates the {@code customerCode} assigned by {@link AssignCustomerCode}. When the
 * computed code collides with an existing owner's code, appends {@code -<n>} using the
 * smallest {@code n} of 2 or more that makes it unique.
 */
public class DeduplicateCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            taken.add(existing.getCustomerCode());
        }
        String base = owner.getCustomerCode();
        String unique = base;
        for (int n = 2; taken.contains(unique); n++) {
            unique = base + "-" + n;
        }
        owner.setCustomerCode(unique);
    }
}
