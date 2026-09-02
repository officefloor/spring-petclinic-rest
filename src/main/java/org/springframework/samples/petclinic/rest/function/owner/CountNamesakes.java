package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners share this owner's first and last name
 * (case-insensitively) at create time, before the new owner is saved.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long count = ownerRepository.findAll().stream()
                .filter(existing -> existing.getFirstName().equalsIgnoreCase(owner.getFirstName())
                        && existing.getLastName().equalsIgnoreCase(owner.getLastName()))
                .count();
        owner.setNamesakeCount((int) count);
    }
}
