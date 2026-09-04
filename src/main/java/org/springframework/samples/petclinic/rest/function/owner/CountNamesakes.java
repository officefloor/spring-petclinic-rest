package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners already shared this owner's first and last name
 * (compared case-insensitively) at creation time, exposed as {@code namesakeCount}.
 * Runs before the new owner is saved, so it counts only pre-existing owners.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = key(owner.getFirstName());
        String lastName = key(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equals(key(existing.getFirstName()))
                    && lastName.equals(key(existing.getLastName()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static String key(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
