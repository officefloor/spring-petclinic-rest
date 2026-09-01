package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * On create, keeps the assigned {@code customerCode} unique: when it already belongs to another
 * owner, appends '-&lt;n&gt;' with the smallest n of 2 or more that makes it unique.
 */
public class DeduplicateOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = owner.getCustomerCode();
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (!Objects.equals(existing.getId(), owner.getId())) {
                taken.add(existing.getCustomerCode());
            }
        }
        String code = base;
        for (int n = 2; taken.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }
}
