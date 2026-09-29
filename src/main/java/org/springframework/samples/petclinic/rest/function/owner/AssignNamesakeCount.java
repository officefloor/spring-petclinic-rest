package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners that share
 * the same first name and last name (compared case-insensitively). Runs before the
 * owner is saved, so the count reflects only the owners that existed before this
 * create.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equalsIgnoreCase(existing.getFirstName())
                    && lastName.equalsIgnoreCase(existing.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }
}
