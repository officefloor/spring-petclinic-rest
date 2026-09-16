package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners already shared this owner's name at creation time:
 * the count of owners with the same firstName and lastName (compared case-insensitively).
 * Computed before the new owner is saved, so it never counts itself.
 */
public class AssignOwnerNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (sameName(existing.getFirstName(), owner.getFirstName())
                    && sameName(existing.getLastName(), owner.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static boolean sameName(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
