package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners sharing the
 * same firstName and lastName as the new owner, compared case-insensitively. Runs after
 * {@link BuildOwner} and before {@link SaveOwner}, so the count reflects the owners that
 * existed before this create and excludes the one being created. The value is fixed at
 * creation time and persisted with the owner.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
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
