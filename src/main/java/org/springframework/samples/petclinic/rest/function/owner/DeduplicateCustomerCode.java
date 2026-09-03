package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * De-duplicates the {@code customerCode} assigned by {@link AssignCustomerCode}: when it
 * already belongs to another owner, append {@code -<n>} with the smallest n of 2 or more
 * that makes it unique, before {@link SaveOwner} runs.
 */
public class DeduplicateCustomerCode {

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
