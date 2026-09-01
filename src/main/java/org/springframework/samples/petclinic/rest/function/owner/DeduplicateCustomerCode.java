package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * De-duplicates the {@code customerCode} assigned by {@link AssignCustomerCode}. When the computed
 * code already belongs to an existing owner, appends {@code -<n>} with the smallest {@code n} of 2 or
 * more that makes it unique. Runs after Assign and before Save, so distinct owners always persist
 * distinct customerCodes.
 */
public class DeduplicateCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = owner.getCustomerCode();
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            taken.add(existing.getCustomerCode());
        }
        String code = base;
        for (int n = 2; taken.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }
}
