package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts how many other owners already share the new owner's last name at the moment of
 * creation, storing the result on the owner as {@code namesakeCount}. Runs before the
 * owner is saved, so the count reflects only the owners that existed beforehand. Last
 * name matching is case-insensitive.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        long namesakes = ownerRepository.findAll().stream()
                .filter(existing -> existing.getLastName() != null
                        && existing.getLastName().equalsIgnoreCase(lastName))
                .count();
        owner.setNamesakeCount((int) namesakes);
    }
}
