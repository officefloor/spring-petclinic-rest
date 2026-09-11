package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of owners that already exist,
 * before this create, sharing the same first name and last name compared
 * case-insensitively. Runs before the owner is saved, so the new owner is not counted.
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
