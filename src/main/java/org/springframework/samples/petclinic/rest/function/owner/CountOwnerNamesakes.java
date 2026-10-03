package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code namesakeCount} on create: the number of existing owners
 * (before this create) that share the same {@code firstName} and {@code lastName},
 * compared case-insensitively. Runs before the owner is saved, so the new owner is not
 * counted; the first owner with a given name therefore gets {@code 0}.
 */
public class CountOwnerNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String firstName = owner.getFirstName();
        String lastName = owner.getLastName();
        int count = (int) ownerRepository.findAll().stream()
                .filter(existing -> firstName.equalsIgnoreCase(existing.getFirstName())
                        && lastName.equalsIgnoreCase(existing.getLastName()))
                .count();
        owner.setNamesakeCount(count);
    }
}
