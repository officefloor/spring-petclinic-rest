package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners that share this owner's
 * {@code firstName} and {@code lastName}, compared case-insensitively. Runs before the new owner is
 * saved, so the count reflects only the owners that existed before this create.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = normalize(owner.getFirstName());
        String lastName = normalize(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equals(normalize(existing.getFirstName()))
                    && lastName.equals(normalize(existing.getLastName()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    /** Lower-case and trim so namesakes are matched case-insensitively. */
    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
