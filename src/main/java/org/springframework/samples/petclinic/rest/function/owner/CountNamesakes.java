package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners already shared the new owner's first and last name (compared
 * case-insensitively) at the moment of creation. Runs before {@link SaveOwner}, so the count
 * excludes the owner currently being created. Mutates the built {@link Owner} in place.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int namesakes = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getFirstName().equalsIgnoreCase(existing.getFirstName())
                    && owner.getLastName().equalsIgnoreCase(existing.getLastName())) {
                namesakes++;
            }
        }
        owner.setNamesakeCount(namesakes);
    }
}
