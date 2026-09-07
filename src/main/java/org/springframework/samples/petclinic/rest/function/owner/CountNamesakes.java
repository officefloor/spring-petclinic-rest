package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records the owner's {@code namesakeCount}: the number of
 * existing owners (before this create) sharing the same first name and last name, compared
 * case-insensitively. Runs before {@link SaveOwner} so the owner being created is not itself
 * counted.
 */
public class CountNamesakes {

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
