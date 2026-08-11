package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the owner's {@code namesakeCount}: the number of existing owners that, at the
 * moment this owner is created, already share the same first name and last name compared
 * case-insensitively (whitespace trimmed). Zero when the name is unique. Runs after
 * {@link BuildOwner} (so the entity, hence its names, exists) and before {@link SaveOwner}
 * (so the new owner is not counted among its own namesakes), and mutates the built
 * {@link Owner} in place.
 */
public class CountNamesakes {

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

    /** Lower-case and trim, treating null as empty, for case-insensitive comparison. */
    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
