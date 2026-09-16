package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners already share the built owner's first and last name,
 * compared case-insensitively. Runs after the owner is built but before it is saved, so the
 * count reflects the owners that existed before this create, and mutates the built
 * {@link Owner} in place.
 */
public class AssignOwnerNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = canonical(owner.getFirstName());
        String lastName = canonical(owner.getLastName());
        int namesakes = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equals(canonical(existing.getFirstName()))
                    && lastName.equals(canonical(existing.getLastName()))) {
                namesakes++;
            }
        }
        owner.setNamesakeCount(namesakes);
    }

    private static String canonical(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
