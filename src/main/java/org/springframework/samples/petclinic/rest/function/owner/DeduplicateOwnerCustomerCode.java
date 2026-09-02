package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Ensures the assigned {@code customerCode} is unique. When it collides with an existing owner's
 * code, appends {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique.
 */
public class DeduplicateOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other != owner) {
                taken.add(other.getCustomerCode());
            }
        }
        String code = owner.getCustomerCode();
        if (taken.contains(code)) {
            int n = 2;
            while (taken.contains(code + "-" + n)) {
                n++;
            }
            owner.setCustomerCode(code + "-" + n);
        }
    }
}
