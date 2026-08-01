package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts how many other owners already share this owner's last name at the moment
 * of creation, and records it on the owner as its namesake count. Runs before the
 * owner is saved, so {@link OwnerRepository#findAll()} yields only the existing
 * owners.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        long namesakes = ownerRepository.findAll().stream()
                .filter(existing -> lastName != null && lastName.equals(existing.getLastName()))
                .count();
        owner.setNamesakeCount((int) namesakes);
    }
}
