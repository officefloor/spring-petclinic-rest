package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records how many other owners already share this owner's last name at the moment
 * it is created. The new owner is not yet saved, so every matching owner counted is
 * a distinct, pre-existing namesake.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        long namesakes = ownerRepository.findByLastName(lastName).stream()
                .filter(existing -> lastName.equals(existing.getLastName()))
                .count();
        owner.setNamesakeCount((int) namesakes);
    }
}
