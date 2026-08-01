package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts how many other owners share the new owner's last name at the moment of
 * creation (before it is saved) and records it as the owner's namesake count.
 */
public class AssignNamesakeCount {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String lastName = owner.getLastName();
        long namesakes = ownerRepository.findAll().stream()
                .filter(o -> lastName == null ? o.getLastName() == null : lastName.equals(o.getLastName()))
                .count();
        owner.setNamesakeCount((int) namesakes);
    }
}
