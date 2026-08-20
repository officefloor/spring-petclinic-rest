package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Stamps {@code namesakeCount} onto the newly built owner: the number of existing owners
 * (before this create) that share the same first name and last name, compared
 * case-insensitively. Runs before {@link SaveOwner}, so the new owner is not yet persisted
 * and therefore not counted among the existing owners.
 */
public class CountOwnerNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = canonical(owner.getFirstName());
        String lastName = canonical(owner.getLastName());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (firstName.equals(canonical(existing.getFirstName()))
                    && lastName.equals(canonical(existing.getLastName()))) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }

    private static String canonical(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
