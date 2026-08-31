package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * On create, records how many existing owners already share this owner's first and last name
 * (compared case-insensitively) as {@code namesakeCount}. Runs before {@code SaveOwner} so the
 * not-yet-persisted owner is excluded from the count.
 */
public class AssignOwnerNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (namesake(existing.getFirstName(), owner.getFirstName())
                    && namesake(existing.getLastName(), owner.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static boolean namesake(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
