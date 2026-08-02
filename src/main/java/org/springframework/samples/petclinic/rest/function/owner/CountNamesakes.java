package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts how many other owners already share this owner's last name at the moment
 * of creation, and records it on the owner. Runs before the owner is saved, so the
 * count excludes this owner itself.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        int namesakes = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (lastName != null && lastName.equals(existing.getLastName())) {
                namesakes++;
            }
        }
        owner.setNamesakeCount(namesakes);
    }
}
