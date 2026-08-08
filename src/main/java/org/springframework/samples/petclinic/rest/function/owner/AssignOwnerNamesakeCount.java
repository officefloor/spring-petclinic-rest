package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many owners already exist that share the new owner's first and last
 * name, compared case-insensitively. Runs before {@link SaveOwner} so the owner
 * being created is not yet persisted and therefore never counts itself. Mutates the
 * {@link Owner} in place so the value is persisted and returned as
 * {@code namesakeCount}.
 */
public class AssignOwnerNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never count the owner itself
            }
            if (equalsIgnoreCase(firstName, existing.getFirstName())
                    && equalsIgnoreCase(lastName, existing.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return a == null ? b == null : a.equalsIgnoreCase(b);
    }
}
