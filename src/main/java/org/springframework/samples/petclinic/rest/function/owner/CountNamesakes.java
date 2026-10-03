package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners that share
 * the same first name and last name, compared case-insensitively. Runs before the owner
 * is saved, so the count reflects the existing owners only (not the owner being created).
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = OwnerNames.normalize(owner.getFirstName());
        String lastName = OwnerNames.normalize(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equals(OwnerNames.normalize(existing.getFirstName()))
                    && lastName.equals(OwnerNames.normalize(existing.getLastName()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }
}
