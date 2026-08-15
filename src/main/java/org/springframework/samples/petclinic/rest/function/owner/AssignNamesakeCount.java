package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners that, at this moment
 * before the new owner is persisted, share the same first name and last name (each compared
 * case-insensitively). Runs after {@link BuildOwner} has produced the {@link Owner} and before
 * {@link SaveOwner} persists it, so {@link OwnerRepository#findAll()} returns only the pre-existing
 * owners and the new owner never counts itself. Mutates the entity in place so {@link SaveOwner}
 * stores the value and later reads return it.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner other : ownerRepository.findAll()) {
            if (equalsIgnoreCase(firstName, other.getFirstName())
                    && equalsIgnoreCase(lastName, other.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return (a == null) ? (b == null) : a.equalsIgnoreCase(b);
    }
}
