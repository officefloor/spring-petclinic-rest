package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners (before this
 * create) that share the same firstName and lastName, compared case-insensitively.
 * Runs before {@code save}, so the new owner is not yet counted.
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

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
