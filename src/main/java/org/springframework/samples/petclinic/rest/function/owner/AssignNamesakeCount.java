package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the namesake count to a newly created owner: the number of other owners
 * that already share the same last name at the moment of creation. The owner being
 * created has not yet been saved, so it is not counted among its own namesakes.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> lastName == null ? existing.getLastName() == null
                        : lastName.equals(existing.getLastName()))
                .count();
        owner.setNamesakeCount((int) count);
    }
}
