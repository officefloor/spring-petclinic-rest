package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records how many existing owners already share this
 * owner's name. {@code namesakeCount} is set to the number of owners present before this create
 * whose firstName and lastName both match, compared case-insensitively with collapsed whitespace.
 * Runs before {@link SaveOwner}, so the owner being created is not counted against itself.
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

    /** Trim, collapse internal whitespace runs to a single space, and lower-case for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
