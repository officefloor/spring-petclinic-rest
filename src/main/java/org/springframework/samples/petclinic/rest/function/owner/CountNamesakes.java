package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records on the newly built owner how many existing owners already share its first and last name,
 * matched case-insensitively. Runs before the owner is saved, so the count reflects the state of the
 * data store immediately before this create.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long count = ownerRepository.findByLastName(owner.getLastName()).stream()
            .filter(existing -> existing.getLastName().equalsIgnoreCase(owner.getLastName())
                && existing.getFirstName().equalsIgnoreCase(owner.getFirstName()))
            .count();
        owner.setNamesakeCount((int) count);
    }
}
