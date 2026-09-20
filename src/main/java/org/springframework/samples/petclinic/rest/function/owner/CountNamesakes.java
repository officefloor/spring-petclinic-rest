package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many existing owners already share the new owner's name. Counts the owners
 * matching on first and last name (see {@link Namesakes#matches}) before the new owner is
 * saved, so the count reflects the state just prior to this create.
 */
public class CountNamesakes {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (Namesakes.matches(existing, owner.getFirstName(), owner.getLastName())) {
                count++;
            }
        }
        owner.setNamesakeCount(count);
    }
}
