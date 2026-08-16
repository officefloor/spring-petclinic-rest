package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount}: the number of existing owners that already share this
 * owner's first name and last name, compared case-insensitively, at the moment of creation.
 *
 * <p>The new owner is not yet saved when this runs, so {@link OwnerRepository#findAll()} returns only
 * the pre-existing owners — the count excludes the owner being created. It is zero when the name is
 * unique. Runs after {@link BuildOwner} (which publishes the new owner) and before {@link SaveOwner}.
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

    /** Lower-cases and trims so the comparison ignores case and surrounding whitespace. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
