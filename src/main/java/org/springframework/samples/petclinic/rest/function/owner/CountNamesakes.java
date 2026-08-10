package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the {@code namesakeCount} on the freshly built {@link Owner} before it is saved:
 * the number of existing owners that share the same first and last name, compared
 * case-insensitively.
 *
 * <p>Runs before {@link SaveOwner} so the new owner is not yet counted — the count reflects
 * only owners that existed before this create.
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

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
